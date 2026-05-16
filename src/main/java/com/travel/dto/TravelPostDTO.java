package com.travel.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPostDTO {

    /**
     * 景点id
     */
    private Long destinationId;


    /**
     * 用户姓名
     */
    private String name;

    /**
     * 标题
     */
    private String title;

    /**
     * 文字描述
     */
    private String content;

    /**
     * 点赞数量
     */
    private Integer liked;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
