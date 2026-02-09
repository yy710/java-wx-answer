package com.yunkesoftware.www.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 签到活动
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sign_activity")
@Schema(name = "SignActivity对象", description = "签到活动")
public class SignActivity implements Serializable {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "介绍")
    @TableField("descr")
    private String descr;

    @Schema(description = "开始时间")
    @TableField("start_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime endTime;

    @Schema(description = "每日奖励积分")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;
}
