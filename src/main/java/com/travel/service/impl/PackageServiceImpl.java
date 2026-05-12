package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.dto.Result;
import com.travel.entity.Package;
import com.travel.mapper.PackageMapper;
import com.travel.entity.SeckillPackage;
import com.travel.service.ISeckillPackageService;
import com.travel.service.IPackageService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.travel.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class PackageServiceImpl extends ServiceImpl<PackageMapper, Package> implements IPackageService {

    @Resource
    private ISeckillPackageService seckillPackageService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryPackageOfShop(Long shopId) {
        // 查询门票信息
        List<Package> aPackages = getBaseMapper().queryPackageOfDestination(shopId);
        // 返回结果
        return Result.ok(aPackages);
    }

    @Override
    @Transactional
    public void addSeckillPackage(Package aPackage) {
        // 保存门票
        save(aPackage);
        // 保存秒杀信息
        SeckillPackage seckillPackage = new SeckillPackage();
        seckillPackage.setPackageId(aPackage.getId());
        seckillPackage.setStock(aPackage.getStock());
        seckillPackage.setBeginTime(aPackage.getBeginTime());
        seckillPackage.setEndTime(aPackage.getEndTime());
        seckillPackageService.save(seckillPackage);

        //保存秒杀库存到redis
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + aPackage.getId(), aPackage.getStock().toString());
    }
}
