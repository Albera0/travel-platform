package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_agent_session")
public class AgentSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 会话标题（比如：东京旅行规划）
     */
    private String sessionTitle;

    /**
     * 状态：1-进行中 2-已完成 0-关闭
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}