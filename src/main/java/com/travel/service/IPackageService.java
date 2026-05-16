package com.travel.service;

import com.travel.dto.Result;
import com.travel.entity.Package;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IPackageService extends IService<Package> {

    Result queryPackageOfDestination(Long destinationId);

    void addSeckillPackage(Package aPackage);

    Result queryPackageByCityForAgent(String city);
}
