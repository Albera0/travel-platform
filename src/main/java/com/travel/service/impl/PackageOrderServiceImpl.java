package com.travel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.travel.dto.Result;
import com.travel.entity.PackageOrder;
import com.travel.mapper.PackageOrderMapper;
import com.travel.service.ISeckillPackageService;
import com.travel.service.IPackageOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.utils.RedisIdWorker;
import com.travel.utils.RedisConstants;
import com.travel.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private volatile boolean running = true;


    @PostConstruct
    public void testRedis() {
        System.out.println("Redis ping: " +
                stringRedisTemplate.getConnectionFactory()
                        .getConnection()
                        .ping());
    }

    @PostConstruct
    private void init() {

        try {

            // 1. 创建stream
            stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
                RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();
                return connection.execute(
                        "XGROUP",
                        serializer.serialize("CREATE"),
                        serializer.serialize("stream.orders"),
                        serializer.serialize("g1"),
                        serializer.serialize("$"),
                        serializer.serialize("MKSTREAM")
                );
            });

            // 2. 创建消费组 g1

            log.info("stream.orders 消费组创建成功");

        } catch (Exception e) {

            // 已存在会报错，忽略即可
            log.info("消费组已存在");
        }

        // 3. 启动异步线程
        SECKILL_ORDER_EXECUTOR.submit(new PackageOrderHandler());
    }

    @PreDestroy
    public void destroy() {
        running = false;
        SECKILL_ORDER_EXECUTOR.shutdownNow();
        try {
            if (!SECKILL_ORDER_EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                log.warn("订单处理线程未在超时时间内退出");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private class PackageOrderHandler implements Runnable {

        String queueName = "stream.orders";

        @Override
        public void run() {
            while (running){
                //1.获取消息队列中的订单信息
                try {
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );

                    //判断消息获取是否成功
                    if (list == null || list.isEmpty()){
                        //如果获取失败，说明没有消息，继续下一次循环
                        continue;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    PackageOrder packageOrder = BeanUtil.fillBeanWithMap(values, new PackageOrder(), true);
                    fillLegacyPackageId(values, packageOrder);
                    //如果获取成功，下单
                    //2.创建订单
                    handlePackageOrder(packageOrder);
                    //ACK确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    if (!running || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    if (isRedisConnectionException(e)) {
                        log.warn("Redis连接异常，订单处理线程稍后重试：{}", e.getMessage());
                        sleepQuietly(1000);
                        continue;
                    }
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList(){
            while (running){
                //1.获取消息队列中的订单信息
                try {
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );

                    //判断消息获取是否成功
                    if (list == null || list.isEmpty()){
                        //如果获取失败，说明pending-list没有消息，继续下一次循环
                        break;
                    }

                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    PackageOrder packageOrder = BeanUtil.fillBeanWithMap(values, new PackageOrder(), true);
                    fillLegacyPackageId(values, packageOrder);
                    //如果获取成功，下单
                    //2.创建订单
                    handlePackageOrder(packageOrder);
                    //ACK确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    if (!running || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    if (isRedisConnectionException(e)) {
                        log.warn("Redis连接异常，暂停处理pending-list：{}", e.getMessage());
                        sleepQuietly(1000);
                        break;
                    }
                    log.error("处理pending-list订单异常", e);
                    sleepQuietly(200);
                }
            }
        }
    }


    private boolean isRedisConnectionException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String className = cause.getClass().getName();
            if (className.contains("RedisConnectionFailureException")
                    || className.contains("RedisSystemException")
                    || className.contains("RedisConnectionException")
                    || className.contains("RedisException")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private void fillLegacyPackageId(Map<Object, Object> values, PackageOrder packageOrder) {
        if (packageOrder.getPackageId() == null && values.containsKey("voucherId")) {
            packageOrder.setPackageId(Long.valueOf(values.get("voucherId").toString()));
        }
    }


    private void handlePackageOrder(PackageOrder packageOrder) {
        //获取用户id
        Long userId = packageOrder.getUserId();
        if (userId == null || packageOrder.getPackageId() == null) {
            log.warn("忽略无效订单消息：{}", packageOrder);
            return;
        }
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
            transactionTemplate.executeWithoutResult(status -> createPackageOrder(packageOrder));
        } finally {
            //释放锁
            lock.unlock();
        }
    }

    @Override
    public Result seckKillPackage(Long voucherId) {
        //获取用户id
        Long userId = UserHolder.getUser().getId();
        Result cacheResult = ensureSeckillStockCached(voucherId);
        if (cacheResult != null) {
            return cacheResult;
        }
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
        //3.返回订单id
        return Result.ok(orderId);
    }

    private Result ensureSeckillStockCached(Long packageId) {
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + packageId;
        String stock = stringRedisTemplate.opsForValue().get(stockKey);
        if (stock != null) {
            return null;
        }

        com.travel.entity.SeckillPackage seckillPackage = seckillPackageService.getById(packageId);
        if (seckillPackage == null) {
            return Result.fail("套餐不存在");
        }
        if (seckillPackage.getStock() == null || seckillPackage.getStock() <= 0) {
            return Result.fail("库存不足");
        }

        stringRedisTemplate.opsForValue().setIfAbsent(stockKey, seckillPackage.getStock().toString());
        return null;
    }


    @Transactional
    public void createPackageOrder(PackageOrder packageOrder) {
        //一人一单
        Long userId = packageOrder.getUserId();

        //查询订单
        int count = query().eq("user_id", userId).eq("package_id", packageOrder.getPackageId()).count();

        //判断是否存在
        if (count > 0) {
            //用户已购买
            log.error("用户已经购买过一次");
            return;
        }

        //5.扣减库存
        boolean success = seckillPackageService.update()
                .setSql("stock = stock - 1")
                .eq("package_id", packageOrder.getPackageId()).gt("stock", 0)
                .update();

        if (!success) {
            //扣减失败
            log.error("库存不足");
            return;
        }



        save(packageOrder);
    }
}


