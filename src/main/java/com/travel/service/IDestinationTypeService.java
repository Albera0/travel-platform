package com.travel.service;

import com.travel.dto.Result;
import com.travel.entity.DestinationType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IDestinationTypeService extends IService<DestinationType> {

    Result getByList();
}
