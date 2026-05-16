package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.dto.PackageDTO;
import com.travel.dto.Result;
import com.travel.dto.TravelPostDTO;
import com.travel.entity.Package;
import com.travel.entity.TravelPost;
import com.travel.mapper.PackageMapper;
import com.travel.entity.SeckillPackage;
import com.travel.service.IDestinationService;
import com.travel.service.ISeckillPackageService;
import com.travel.service.IPackageService;
import com.travel.utils.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.travel.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class PackageServiceImpl extends ServiceImpl<PackageMapper, Package> implements IPackageService {

    @Resource
    private ISeckillPackageService seckillPackageService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IDestinationService  destinationService;

    @Override
    public Result queryPackageOfDestination(Long destinationId) {
        // 查询门票信息
        List<Package> aPackages = getBaseMapper().queryPackageOfDestination(destinationId);
        // 返回结果
        return Result.ok(aPackages);
    }

    @Override
    @Transactional
    public void addSeckillPackage(Package aPackage) {
        // 保存门票
        save(aPackage);
        // 保存秒杀信息
        SeckillPackage seckillPackage = new SeckillPackage();
        seckillPackage.setPackageId(aPackage.getId());
        seckillPackage.setStock(aPackage.getStock());
        seckillPackage.setBeginTime(aPackage.getBeginTime());
        seckillPackage.setEndTime(aPackage.getEndTime());
        seckillPackageService.save(seckillPackage);

        //保存秒杀库存到redis
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + aPackage.getId(), aPackage.getStock().toString());
    }

    /**
     * Agent专用：根据景点名称获取门票
     *
     * 核心流程：
     * 1. 名称 → ID（景点服务）
     * 2. ID → package查询（数据库）
     * 3. enrich秒杀优惠券
     */
    @Override
    public Result queryPackageByCityForAgent(String city) {

        // 1. 名称 → ID（Result.data）
        Result idResult = destinationService.getIdByCity(city);

        List<Long> destinationIds = (List<Long>) idResult.getData();

        if (destinationIds == null || destinationIds.isEmpty()) {
            return Result.ok(null);
        }

        // 2. 查询门票
        Page<Package> page = this.lambdaQuery()
                .in(Package::getDestinationId, destinationIds)
                .orderByAsc(Package::getPayValue)
                .page(new Page<>(1, SystemConstants.MAX_PAGE_SIZE));

        List<Package> records = page.getRecords();

        // 3. 构建DTO对象
        List<PackageDTO> packageDTOS = new ArrayList<>(records.size());
        for (Package aPackage : records) {
            // 1. 查询秒杀信息（按 packageId）
            Result seckillResult = seckillPackageService.querySeckillPackageByPackageId(aPackage.getId());
            SeckillPackage seckillPackage = (SeckillPackage) seckillResult.getData();

            PackageDTO.SeckillInfo seckillInfo = null;
            if (seckillResult.getData() != null) {
                seckillInfo = PackageDTO.SeckillInfo.builder()
                        .seckill(Boolean.TRUE)
                        .stock(seckillPackage.getStock())
                        .beginTime(seckillPackage.getBeginTime())
                        .endTime(seckillPackage.getEndTime())
                        .build();
            }

            // 2. 组装 DTO
            PackageDTO packageDTO = PackageDTO.builder()
                    .destinationId(aPackage.getDestinationId())
                    .title(aPackage.getTitle())
                    .payValue(aPackage.getPayValue())
                    .actualValue(aPackage.getActualValue())
                    .stock(aPackage.getStock())
                    .status(aPackage.getStatus())
                    .beginTime(aPackage.getBeginTime())
                    .endTime(aPackage.getEndTime())
                    .seckillInfo(seckillInfo)
                    .build();
            packageDTOS.add(packageDTO);
        }

        // 4. 返回
        return Result.ok(packageDTOS);
    }
}
