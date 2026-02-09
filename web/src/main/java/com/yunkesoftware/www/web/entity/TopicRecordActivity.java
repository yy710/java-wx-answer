package com.yunkesoftware.www.web.entity;

import com.yunkesoftware.www.query.PageCurrency;

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
 * 答题活动记录
 * </p>
 *
 * @author yk
 * @since 2026-01-19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record_activity")
@Schema(name = "TopicRecordActivity对象", description = "答题活动记录")
public class TopicRecordActivity extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "答题活动id")
    @TableField("topic_activity_id")
    private String topicActivityId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "奖励积分数")
    @TableField("total_reward_amount")
    private BigDecimal totalRewardAmount;

    @Schema(description = "昵称")
    @TableField(exist = false)
    private String nickName;
}
