package com.travel.controller;


import com.travel.dto.Result;
import com.travel.entity.DestinationType;
import com.travel.service.IDestinationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/destination-type")
public class DestinationTypeController {
    @Resource
    private IDestinationTypeService typeService;
    @Autowired
    private IDestinationTypeService iDestinationTypeService;

    @GetMapping("list")
    public Result queryTypeList() {
        List<DestinationType> typeList = typeService
                .query().orderByAsc("sort").list();
        return iDestinationTypeService.getByList();
    }
}
