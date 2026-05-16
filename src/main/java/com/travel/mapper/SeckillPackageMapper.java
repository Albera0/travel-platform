package com.travel.mapper;

import com.travel.entity.SeckillPackage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 Mapper 接口
 * </p>

 */
public interface SeckillPackageMapper extends BaseMapper<SeckillPackage> {

    SeckillPackage querySeckillPackageByPackageId(@Param("packageId") Long packageId);

}
