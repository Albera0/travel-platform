package com.travel.service;

import com.travel.dto.Result;
import com.travel.entity.Destination;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IDestinationService extends IService<Destination> {

    Result queryById(Long id);

    Result update(Destination destination);

    Result queryDestinationByType(Integer typeId, Integer current, Double x, Double y);
}
