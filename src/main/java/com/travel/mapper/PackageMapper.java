package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Package;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>

 */
public interface PackageMapper extends BaseMapper<Package> {

    List<Package> queryPackageOfDestination(@Param("packageId") Long destinationId);
}
