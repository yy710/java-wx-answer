package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

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
 * 答题记录题目
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record_topic")
@Schema(name = "TopicRecordTopic对象", description = "答题记录题目")
public class TopicRecordTopic extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "答题记录id")
    @TableField("topic_record_id")
    @ExcelIgnore
    private String topicRecordId;

    @Schema(description = "题目id")
    @TableField("topic_id")
    @ExcelIgnore
    private String topicId;

    @Schema(description = "题目标题")
    @TableField("topic_title")
    @ExcelProperty("题目标题")
    private String topicTitle;

    @Schema(description = "奖励积分")
    @TableField("reward_amount")
    @ExcelProperty("奖励积分")
    private BigDecimal rewardAmount;

    @Schema(description = "答案是否正确")
    @TableField("answer_flag")
    @ExcelProperty("答案是否正确")
    private Boolean answerFlag;

    @TableField(exist = false)
    private List<TopicRecordTopicItem> topicRecordTopicItemList;
}
