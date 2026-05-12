package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_travel_plan")
public class TravelPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对应任务ID
     */
    private Long taskId;

    /**
     * 版本号（支持多版本生成）
     */
    private Integer version;

    /**
     * 预计花费
     */
    private Long estimatedCost;

    /**
     * 使用的AI模型（gpt / local / etc）
     */
    private String aiModel;

    /**
     * 是否当前版本：1-是 0-否
     */
    private Integer isCurrent;

    /**
     * 计划内容（可以是JSON或文本）
     */
    private String planContent;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}