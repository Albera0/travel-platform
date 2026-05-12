package com.travel.service;

import com.travel.dto.Result;
import com.travel.entity.PackageOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IPackageOrderService extends IService<PackageOrder> {

    Result seckKillPackage(Long voucherId);

    void createPackageOrder(PackageOrder packageOrder);
}
