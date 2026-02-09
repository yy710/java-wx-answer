package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户答题记录
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record")
@Schema(name = "TopicRecord对象", description = "用户答题记录")
public class TopicRecord {

    @TableId("id")
    private String id;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "答题活动id")
    @TableField("topic_activity_id")
    private String topicActivityId;

    @Schema(description = "答题活动名称")
    @TableField("topic_activity_title")
    private String topicActivityTitle;

    @Schema(description = "答题线路id")
    @TableField("topic_line_id")
    @NotBlank(message = "未知答题线路")
    private String topicLineId;

    @Schema(description = "答题线路名称")
    @TableField("topic_line_title")
    private String topicLineTitle;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "耗时秒")
    @TableField("used_time")
    @NotNull(message = "答题耗时不能为空")
    private Integer usedTime;

    @Schema(description = "答对题目数")
    @TableField("right_num")
    private Integer rightNum;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;


    @Schema(description = "奖励积分总数")
    @TableField(exist = false)
    private BigDecimal totalRewardAmount;

    @Schema(description = "排名")
    @TableField(exist = false)
    private long rankNum;

}
