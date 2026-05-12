package com.travel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.travel.dto.Result;
import com.travel.entity.PackageOrder;
import com.travel.mapper.PackageOrderMapper;
import com.travel.service.ISeckillPackageService;
import com.travel.service.IPackageOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.utils.RedisIdWorker;
import com.travel.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
@Slf4j
public class PackageOrderServiceImpl extends ServiceImpl<PackageOrderMapper, PackageOrder> implements IPackageOrderService {

    @Resource
    private ISeckillPackageService seckillPackageService;
    @Autowired
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init(){
        SECKILL_ORDER_EXECUTOR.submit(new PackageOrderHandler());
    }

    private class PackageOrderHandler implements Runnable {

        String queueName = "stream.orders";

        @Override
        public void run() {
            while (true){
                //1.获取消息队列中的订单信息
                try {
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );

                    //判断消息获取是否成功
                    if (list.isEmpty() || list == null){
                        //如果获取失败，说明没有消息，继续下一次循环
                        continue;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    PackageOrder packageOrder = BeanUtil.fillBeanWithMap(values, new PackageOrder(), true);
                    //如果获取成功，下单
                    //2.创建订单
                    handlePackageOrder(packageOrder);
                    //ACK确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList(){
            while (true){
                //1.获取消息队列中的订单信息
                try {
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );

                    //判断消息获取是否成功
                    if (list.isEmpty() || list == null){
                        //如果获取失败，说明pending-list没有消息，继续下一次循环
                        break;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    PackageOrder packageOrder = BeanUtil.fillBeanWithMap(values, new PackageOrder(), true);
                    //如果获取成功，下单
                    //2.创建订单
                    handlePackageOrder(packageOrder);
                    //ACK确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理pending-list订单异常", e);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }


    private void handlePackageOrder(PackageOrder packageOrder) {
        //获取用户id
        Long userId = packageOrder.getUserId();
        //创建锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);

        //获取锁
        boolean thisLock = lock.tryLock();

        if (!thisLock){
            //获取锁失败，返回错误或重试
            log.error("不允许重复下单");
            return;
        }
        //获取事务有关的代理对象
        try {
            proxy.createPackageOrder(packageOrder);
        } finally {
            //释放锁
            lock.unlock();
        }
    }

    private IPackageOrderService proxy;

    @Override
    public Result seckKillPackage(Long voucherId) {
        //获取用户id
        Long userId = UserHolder.getUser().getId();
        //获取订单id
        long orderId = redisIdWorker.nextId("order");

        //1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId)
        );

        //2.判断结果是否为0
        int r = result.intValue();
        if (r != 0) {
            //2.1 不为0，没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "重复下单");
        }

        //获取代理对象
        proxy = (IPackageOrderService) AopContext.currentProxy();

        //3.返回订单id
        return Result.ok(orderId);
    }


    @Transactional
    public void createPackageOrder(PackageOrder packageOrder) {
        //一人一单
        Long userId = packageOrder.getUserId();

        //查询订单
        int count = query().eq("user_id", userId).eq("voucher_id", packageOrder.getPackageId()).count();

        //判断是否存在
        if (count > 0) {
            //用户已购买
            log.error("用户已经购买过一次");
            return;
        }

        //5.扣减库存
        boolean success = seckillPackageService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", packageOrder.getPackageId()).gt("stock", 0)
                .update();

        if (!success) {
            //扣减失败
            log.error("库存不足");
            return;
        }



        save(packageOrder);
    }
}


