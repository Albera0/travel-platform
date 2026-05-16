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
    private IPackageService packageService;

    /**
     * 新增普通门票
     * @param Package 优惠门票信息
     * @return 优惠门票id
     */
    @PostMapping
    public Result addPackage(@RequestBody Package Package) {
        packageService.save(Package);
        return Result.ok(Package.getId());
    }

    /**
     * 新增秒杀门票
     * @param Package 优惠门票信息，包含秒杀信息
     * @return 优惠门票id
     */
    @PostMapping("seckill")
    public Result addSeckillPackage(@RequestBody Package Package) {
        packageService.addSeckillPackage(Package);
        return Result.ok(Package.getId());
    }

    /**
     * 查询景点的优惠门票列表
     * @param destinationId 景点id
     * @return 优惠门票列表
     */
    @GetMapping("/list/{destinationId}")
    public Result queryPackageOfShop(@PathVariable("destinationId") Long destinationId) {
       return packageService.queryPackageOfDestination(destinationId);
    }

    /**
     * Agent专用接口：
     * 流程：
     * 1. destinationService：景点名称 → 景点ID
     * 2. packageService：根据景点ID查询门票
     * 3. enrich：秒杀门票
     */
    @GetMapping("/agent/package-by-city")
    public Result queryHotByDestinationForAgent(
            @RequestParam("city") String city) {
        return packageService.queryPackageByCityForAgent(city);
    }
}
