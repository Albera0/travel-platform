package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.TravelTask;
import com.travel.mapper.TravelTaskMapper;
import com.travel.service.ITravelTaskService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class TravelTaskServiceImpl extends ServiceImpl<TravelTaskMapper, TravelTask> implements ITravelTaskService {

}
