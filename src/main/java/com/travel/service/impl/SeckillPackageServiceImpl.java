package com.travel.service.impl;

import com.travel.entity.SeckillPackage;
import com.travel.mapper.SeckillPackageMapper;
import com.travel.service.ISeckillPackageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 */
@Service
public class SeckillPackageServiceImpl extends ServiceImpl<SeckillPackageMapper, SeckillPackage> implements ISeckillPackageService {

}
