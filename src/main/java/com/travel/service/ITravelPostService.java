package com.travel.service;

import com.travel.dto.Result;
import com.travel.entity.TravelPost;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface ITravelPostService extends IService<TravelPost> {

    Result queryHotDestination(Integer current);

    Result queryDestinationById(Long id);

    Result likeDestination(Long id);

    Result queryDestinationLikes(Long id);

    Result saveDestination(TravelPost travelPost);

    Result queryDestinationOfFollow(Long max, Integer offset);
}
