package com.travel.controller;


import com.travel.dto.Result;
import com.travel.entity.Package;
import com.travel.service.IPackageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 */
@RestController
@RequestMapping("/package")
public class PackageController {

    @Resource
    private IPackageService voucherService;

    /**
     * 新增普通门票
     * @param Package 优惠门票信息
     * @return 优惠门票id
     */
    @PostMapping
    public Result addPackage(@RequestBody Package Package) {
        voucherService.save(Package);
        return Result.ok(Package.getId());
    }

    /**
     * 新增秒杀门票
     * @param Package 优惠门票信息，包含秒杀信息
     * @return 优惠门票id
     */
    @PostMapping("seckill")
    public Result addSeckillPackage(@RequestBody Package Package) {
        voucherService.addSeckillPackage(Package);
        return Result.ok(Package.getId());
    }

    /**
     * 查询店铺的优惠门票列表
     * @param shopId 店铺id
     * @return 优惠门票列表
     */
    @GetMapping("/list/{shopId}")
    public Result queryPackageOfShop(@PathVariable("shopId") Long shopId) {
       return voucherService.queryPackageOfShop(shopId);
    }
}
