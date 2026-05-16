package com.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDTO {
    /**
     * 景点id
     */
    private Long destinationId;

    /**
     * 门票标题
     */
    private String title;

    /**
     * 支付金额
     */
    private Long payValue;

    /**
     * 抵扣金额
     */
    private Long actualValue;

    /**
     * 门票状态
     */
    private Integer status;

    /**
     * 库存
     */
    private Integer stock;

    private SeckillInfo seckillInfo;

    /**
     * 生效时间
     */
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    private LocalDateTime endTime;


    /**
     * 秒杀信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeckillInfo {

        /**
         * 是否秒杀门票
         */
        private Boolean seckill;

        /**
         * 秒杀库存
         */
        private Integer stock;

        /**
         * 秒杀开始时间
         */
        private LocalDateTime beginTime;

        /**
         * 秒杀结束时间
         */
        private LocalDateTime endTime;
    }

}
