package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.TravelPlan;
import com.travel.mapper.TravelPlanMapper;
import com.travel.service.ITravelPlanService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class TravelPlanServiceImpl extends ServiceImpl<TravelPlanMapper, TravelPlan> implements ITravelPlanService {

}
