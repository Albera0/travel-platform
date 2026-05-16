package com.travel.service.impl;

import com.travel.dto.Result;
import com.travel.entity.Package;
import com.travel.entity.SeckillPackage;
import com.travel.mapper.SeckillPackageMapper;
import com.travel.service.ISeckillPackageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 */
@Service
public class SeckillPackageServiceImpl extends ServiceImpl<SeckillPackageMapper, SeckillPackage> implements ISeckillPackageService {

    @Override
    public Result querySeckillPackageByPackageId(Long packageId) {
        // 查询门票信息
        SeckillPackage seckillPackage = getBaseMapper().querySeckillPackageByPackageId(packageId);
        // 返回结果
        return Result.ok(seckillPackage);
    }
}
