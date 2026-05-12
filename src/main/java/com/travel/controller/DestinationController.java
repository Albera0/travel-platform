package com.travel.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.Result;
import com.travel.entity.Destination;
import com.travel.service.IDestinationService;
import com.travel.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/destination")
public class DestinationController {

    @Resource
    public IDestinationService destinationService;

    /**
     * 根据id查询景点信息
     * @param id 景点id
     * @return 景点详情数据
     */
    @GetMapping("/{id}")
    public Result queryDestinationById(@PathVariable("id") Long id) {

        return destinationService.queryById(id);
    }

    /**
     * 新增景点信息
     * @param destination 景点数据
     * @return 景点id
     */
    @PostMapping
    public Result saveDestination(@RequestBody Destination destination) {
        // 写入数据库
        destinationService.save(destination);
        // 返回店铺id
        return Result.ok(destination.getId());
    }

    /**
     * 更新景点信息
     * @param destination 数据
     * @return 无
     */
    @PutMapping
    public Result updateDestination(@RequestBody Destination destination) {
        // 写入数据库
        return destinationService.update(destination);
    }

    /**
     * 根据景点类型分页查询景点信息
     * @param typeId 景点类型
     * @param current 页码
     * @return 景点列表
     */
    @GetMapping("/of/type")
    public Result queryDestinationByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y
    ) {
        return destinationService.queryDestinationByType(typeId, current, x, y);
    }

    /**
     * 根据景点名称关键字分页查询景点信息
     * @param name 景点名称关键字
     * @param current 页码
     * @return 景点列表
     */
    @GetMapping("/of/name")
    public Result queryDestinationByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Destination> page = destinationService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }
}
