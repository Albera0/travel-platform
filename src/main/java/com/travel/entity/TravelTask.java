package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_travel_task")
public class TravelTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 任务编号（用于追踪，比如 TASK_001）
     */
    private String taskNo;

    /**
     * 目的地（如：东京）
     */
    private String destination;

    /**
     * 天数
     */
    private Integer days;

    /**
     * 预算（单位：分/元看你系统统一）
     */
    private Long budget;

    /**
     * 用户原始输入
     */
    private String userPrompt;

    /**
     * 状态：1-生成中 2-完成 3-失败
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}