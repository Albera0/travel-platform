package com.travel.service;

import com.travel.dto.Result;
import com.travel.entity.SeckillPackage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务类
 * </p>
 */
public interface ISeckillPackageService extends IService<SeckillPackage> {

    Result querySeckillPackageByPackageId(Long packageId);
}
