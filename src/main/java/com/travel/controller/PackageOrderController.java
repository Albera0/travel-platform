package com.travel.controller;


import com.travel.dto.Result;
import com.travel.service.IPackageOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 */
@RestController
@RequestMapping("/package-order")
public class PackageOrderController {

    @Resource
    private IPackageOrderService packageOrderService;

    @PostMapping("seckill/{id}")
    public Result seckillPackage(@PathVariable("id") Long packageId) {
        return packageOrderService.seckKillPackage(packageId);
    }
}
