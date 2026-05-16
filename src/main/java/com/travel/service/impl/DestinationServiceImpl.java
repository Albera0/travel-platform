package com.travel.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.DestinationDTO;
import com.travel.dto.Result;
import com.travel.entity.Destination;
import com.travel.mapper.DestinationMapper;
import com.travel.service.IDestinationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.utils.CacheClient;
import com.travel.utils.RedisData;
import com.travel.utils.SystemConstants;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.travel.utils.RedisConstants.*;
import static com.travel.utils.RedisConstants.CACHE_DESTINATION_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class DestinationServiceImpl extends ServiceImpl<DestinationMapper, Destination> implements IDestinationService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        // 缓存穿透

        //逻辑过期解决缓存击穿
        Destination destination = cacheClient.
                queryWithLogicalExpire(CACHE_DESTINATION_KEY, id, Destination.class, this::getById, CACHE_DESTINATION_TTL, TimeUnit.MINUTES);

        if (destination == null) {
            return  Result.fail("景点不存在");
        }

        //7.返回
        return Result.ok(destination);
    }
    

  public void saveDestination2Redis(Long id, Long expireSeconds){
        //1.查询景点数据
        Destination destination = getById(id);

        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(destination);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));

        //3.写入Redis
        stringRedisTemplate.opsForValue().set(CACHE_DESTINATION_KEY+id, JSONUtil.toJsonStr(redisData));

    }

    @Override
    @Transactional
    public Result update(Destination destination) {
        if(destination.getId()==null){
            return Result.fail("景点id不能为空");
        }
        //1.更新数据库
        updateById(destination);

        //2.删除缓存
        stringRedisTemplate.delete(CACHE_DESTINATION_KEY + destination.getId());

        return Result.ok();
    }

    @Override
    public Result queryDestinationByType(Integer typeId, Integer current, Double x, Double y) {
      //判断是否需要根据坐标查询
        if (x != null || y != null ){
            //不需要分页查询，根据数据库查询
            // 根据类型分页查询
            Page<Destination> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
            // 返回数据
        }

      //计算分页参数
        int from = (current -1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

      //查询redis，按照距离排序，分页
        String key = DESTINATION_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );

        //解析出id
        if (results == null) {
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from){
            return Result.ok(Collections.emptyList());
        }
        //截取从from到end的部分
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });

        //根据id查询景点
        String idStr = StrUtil.join(",", ids);
        List<Destination> destinations = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Destination destination : destinations) {
            destination.setDistance(distanceMap.get(destination.getId().toString()).getValue());
        }

        //返回
      return Result.ok(destinations);
    }

    public Result getDestinationForAgent(String city) {

        // 1. 查数据库（核心数据）
        List<Destination> destinations = query()
                .eq("area", city)
                .list();

        List<DestinationDTO> destinationDTOS = new ArrayList<>(destinations.size());
        for  (Destination destination : destinations) {
            DestinationDTO destinationDTO = DestinationDTO.builder()
                    .name(destination.getName())
                    .sold(destination.getSold())
                    .area(destination.getArea())
                    .x(destination.getX())
                    .y(destination.getY())
                    .score(destination.getScore())
                    .avgPrice(destination.getAvgPrice())
                    .openHours(destination.getOpenHours())
                    .typeId(destination.getTypeId())
                    .build();
            destinationDTOS.add(destinationDTO);
        }
        return Result.ok(destinationDTOS);
    }

    @Override
    public Result getIdByCity(String city) {

        if (city == null || city.isEmpty()) {
            return Result.ok(null);
        }

        // 1. 数据库查询（名称匹配）
        List<Destination> destinations = query()
                .eq("area", city)
                .list();

        // 2. 未找到
        if (destinations == null) {
            return Result.ok(null);
        }

        //3. 只提取id
        List<Long> ids = new ArrayList<>(destinations.size());
        for  (Destination destination : destinations) {
            ids.add(destination.getId());
        }

        // 4. 返回ID
        return Result.ok(ids);
    }
}
