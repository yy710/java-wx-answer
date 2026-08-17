package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 所有正向奖励的幂等占位。 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("reward_claims")
public class RewardClaim {
    @TableId("id")
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("event_id")
    private String eventId;
    @TableField("event_type")
    private Integer eventType;
    @TableField("task_date")
    private LocalDate taskDate;
    @TableField("requested_points")
    private BigDecimal requestedPoints;
    @TableField("awarded_points")
    private BigDecimal awardedPoints;
    @TableField("status")
    private String status;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("claimed_at")
    private LocalDateTime claimedAt;
}
