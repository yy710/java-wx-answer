package com.yunkesoftware.www.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频活动-视频信息
 * </p>
 *
 * @author yk
 * @since 2026-02-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("video_activity_video")
@Schema(name = "VideoActivityVideo对象", description = "视频活动-视频信息")
public class VideoActivityVideo extends PageCurrency {

    private static final long serialVersionUID = 1L;

    @Schema(description = "视频活动id")
    @TableField("video_activity_id")
    private Long videoActivityId;

    @Schema(description = "视频id")
    @TableField("video_id")
    private Long videoId;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "观看最小时长")
    @TableField("min_time")
    private Integer minTime;
}
