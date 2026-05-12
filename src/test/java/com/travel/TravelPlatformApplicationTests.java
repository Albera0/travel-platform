package com.travel;

import com.travel.entity.Destination;
import com.travel.service.impl.DestinationServiceImpl;
import com.travel.utils.CacheClient;
import com.travel.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.travel.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.travel.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class TravelPlatformApplicationTests {

    @Resource
    private CacheClient cacheClient;

    @Resource
    private DestinationServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService es =  Executors.newFixedThreadPool(500);
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(300);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            countDownLatch.countDown();
        };
        long begin = System.currentTimeMillis();

        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }

        countDownLatch.await();
        long end = System.currentTimeMillis();

        System.out.println("time = " + (end - begin));
    }

    @Test
    void testSaveShop() {
        Destination destination = shopService.getById(1L);

        cacheClient.setWithLogicExpire(CACHE_SHOP_KEY + 1L, destination, 10L, TimeUnit.SECONDS);
    }

    @Test
    void loadShopData() {
        //查询店铺信息
        List<Destination> list = shopService.list();

        //把店铺分组，按照typeId分组
        Map<Long, List<Destination>> map = list.stream().collect(Collectors.groupingBy(Destination::getTypeId));

        //分批写入redis
        for (Map.Entry<Long, List<Destination>> entry : map.entrySet()) {
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            List<Destination> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            for (Destination destination : value) {
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        destination.getId().toString(), new Point(destination.getX(), destination.getY())));
            }

            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }

}
