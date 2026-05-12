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

    Result queryPackageOfShop(Long shopId);

    void addSeckillPackage(Package aPackage);
}
