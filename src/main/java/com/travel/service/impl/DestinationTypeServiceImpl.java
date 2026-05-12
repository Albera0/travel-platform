package com.travel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.travel.dto.Result;
import com.travel.entity.DestinationType;
import com.travel.mapper.DestinationTypeMapper;
import com.travel.service.IDestinationTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class DestinationTypeServiceImpl extends ServiceImpl<DestinationTypeMapper, DestinationType> implements IDestinationTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IDestinationTypeService typeService;

    @Override
    public Result getByList() {
        //1.从redis查询景点类型缓存
        String shopTypeList = stringRedisTemplate.opsForValue().get("cache:shopType");

        //2.判断是否存在
        if(StrUtil.isNotBlank(shopTypeList)){
            //3.存在则直接返回
            List<DestinationType> destinationType = JSONUtil.toList(shopTypeList, DestinationType.class);
            return Result.ok(destinationType);
        }

        //4.不存在，查询数据库
        List<DestinationType> typeList = typeService
                .query().orderByAsc("sort").list();

        //5.不存在，返回错误
        if(CollUtil.isEmpty(typeList)){
            return Result.fail("商铺类型不存在");
        }

        //6.存在，写入redis
        stringRedisTemplate.opsForValue().set("cache:shopType", JSONUtil.toJsonStr(typeList));

        //7.返回
        return Result.ok(typeList);

    }
}
