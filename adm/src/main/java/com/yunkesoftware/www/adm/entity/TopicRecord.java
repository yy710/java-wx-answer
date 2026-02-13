package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户答题记录
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record")
@Schema(name = "TopicRecord对象", description = "用户答题记录")
public class TopicRecord extends PageCurrency {

    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "用户id")
    @TableField("user_id")
    @ExcelIgnore
    private String userId;

    @Schema(description = "答题活动id")
    @TableField("topic_activity_id")
    @ExcelIgnore
    private String topicActivityId;

    @Schema(description = "答题活动名称")
    @TableField("topic_activity_title")
    @ExcelProperty("答题活动名称")
    private String topicActivityTitle;

    @Schema(description = "答题线路id")
    @TableField("topic_line_id")
    @ExcelIgnore
    private String topicLineId;

    @Schema(description = "答题线路名称")
    @TableField("topic_line_title")
    @ExcelProperty("答题线路名称")
    private String topicLineTitle;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    @ExcelProperty("奖励积分数")
    private BigDecimal rewardAmount;

    @Schema(description = "耗时秒")
    @TableField("used_time")
    @ExcelProperty("耗时秒")
    private Integer usedTime;

    @Schema(description = "答对题目数")
    @TableField("right_num")
    @ExcelProperty("答对题目数")
    private Integer rightNum;

    @Schema(description = "题目总数")
    @TableField("total_num")
    private Integer totalNum;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "用户昵称")
    @TableField(exist = false)
    private String nickName;

    @TableField(exist = false)
    private List<TopicRecordTopic> topicRecordTopicList;
}
