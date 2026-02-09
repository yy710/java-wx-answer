package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 线路-题目信息
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_line_topic")
@Schema(name = "TopicLineTopic对象", description = "线路-题目信息")
public class TopicLineTopic {

    @Schema(description = "线路id")
    @TableField("topic_line_id")
    private String topicLineId;

    @Schema(description = "题目id")
    @TableField("topic_id")
    private String topicId;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    @ExcelProperty("奖励积分数")
    private BigDecimal rewardAmount;

    @Schema(description = "排序")
    @TableField("seq")
    @ExcelProperty("排序")
    private Integer seq;

    @TableField(exist = false)
    @Schema(description = "题目名称")
    @ExcelProperty("题目名称")
    private String topicName;
}
