package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 题目信息
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic")
@Schema(name = "Topic对象", description = "题目信息")
public class Topic implements Serializable {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;


    @Schema(description = "题目选项")
    @TableField(exist = false)
    private List<TopicItem> topicItemList;

    @Schema(description = "1-首次答题 2-答错 3-答对")
    @TableField(exist = false)
    private Integer againType = 1;

}
