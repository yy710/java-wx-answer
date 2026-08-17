package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunkesoftware.www.query.PageCurrency;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@TableName("daily_task_config")
public class DailyTaskConfig extends PageCurrency {
    @TableId("id") private Integer id;
    @TableField("enabled") private Boolean enabled;
    @TableField("quiz_enabled") private Boolean quizEnabled;
    @TableField("video_enabled") private Boolean videoEnabled;
    @TableField("affair_enabled") private Boolean affairEnabled;
    @TableField("share_enabled") private Boolean shareEnabled;
    @TableField("wallet_max_points") private BigDecimal walletMaxPoints;
    @TableField("daily_positive_max_points") private BigDecimal dailyPositiveMaxPoints;
    @TableField("quiz_daily_attempts") private Integer quizDailyAttempts;
    @TableField("quiz_question_count") private Integer quizQuestionCount;
    @TableField("quiz_reward_per_correct") private BigDecimal quizRewardPerCorrect;
    @TableField("quiz_time_limit_seconds") private Integer quizTimeLimitSeconds;
    @TableField("video_daily_count") private Integer videoDailyCount;
    @TableField("video_reward_points") private BigDecimal videoRewardPoints;
    @TableField("video_min_watch_ratio") private BigDecimal videoMinWatchRatio;
    @TableField("affair_daily_count") private Integer affairDailyCount;
    @TableField("affair_reward_points") private BigDecimal affairRewardPoints;
    @TableField("share_daily_count") private Integer shareDailyCount;
    @TableField("share_reward_points") private BigDecimal shareRewardPoints;
    @TableField("poster_cache_days") private Integer posterCacheDays;
    @TableField("version") private Long version;
    @TableField("updated_by") private String updatedBy;
}
