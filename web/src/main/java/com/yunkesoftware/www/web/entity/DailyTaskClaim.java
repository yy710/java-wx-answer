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

@Getter
@Setter
@Accessors(chain = true)
@TableName("daily_task_claim")
public class DailyTaskClaim {
    @TableId("id")
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("task_date")
    private LocalDate taskDate;
    @TableField("task_type")
    private String taskType;
    @TableField("source")
    private String source;
    @TableField("requested_points")
    private BigDecimal requestedPoints;
    @TableField("awarded_points")
    private BigDecimal awardedPoints;
    @TableField("award_reason")
    private String awardReason;
    @TableField("wallet_record_id")
    private String walletRecordId;
    @TableField("completed_at")
    private LocalDateTime completedAt;
}
