package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import java.math.BigDecimal;

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
 * @since 2026-01-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record_topic")
@Schema(name = "TopicRecordTopic对象", description = "答题记录题目")
public class TopicRecordTopic {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "答题记录id")
    @TableField("topic_record_id")
    private String topicRecordId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "题目id")
    @TableField("topic_id")
    private String topicId;

    @Schema(description = "题目标题")
    @TableField("topic_title")
    private String topicTitle;

    @Schema(description = "奖励积分")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "答案是否正确")
    @TableField("answer_flag")
    private Boolean answerFlag;
}
