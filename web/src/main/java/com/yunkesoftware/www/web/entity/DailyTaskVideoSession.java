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
@TableName("daily_task_video_session")
public class DailyTaskVideoSession {
    @TableId("id")
    private String id;
    @TableField("user_id")
    private String userId;
    @TableField("task_date")
    private LocalDate taskDate;
    @TableField("video_id")
    private String videoId;
    @TableField("video_duration_seconds")
    private Integer videoDurationSeconds;
    @TableField("min_watch_ratio")
    private BigDecimal minWatchRatio;
    @TableField("required_watch_seconds")
    private Integer requiredWatchSeconds;
    @TableField("credited_watch_seconds")
    private Integer creditedWatchSeconds;
    @TableField("last_position_seconds")
    private BigDecimal lastPositionSeconds;
    @TableField("last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;
    @TableField("status")
    private String status;
    @TableField("version")
    private Long version;
    @TableField("started_at")
    private LocalDateTime startedAt;
    @TableField("claimed_at")
    private LocalDateTime claimedAt;
}
