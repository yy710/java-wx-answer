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
@TableName("daily_task_quiz_session")
public class DailyTaskQuizSession {
    @TableId("id")
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("task_date")
    private LocalDate taskDate;
    @TableField("status")
    private String status;
    @TableField("question_count")
    private Integer questionCount;
    @TableField("reward_per_correct")
    private BigDecimal rewardPerCorrect;
    @TableField("time_limit_seconds")
    private Integer timeLimitSeconds;
    @TableField("started_at")
    private LocalDateTime startedAt;
    @TableField("deadline_at")
    private LocalDateTime deadlineAt;
    @TableField("submitted_at")
    private LocalDateTime submittedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
