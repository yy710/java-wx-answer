package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

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
public class VideoActivityVideo {

    @Schema(description = "视频活动id")
    @TableField("video_activity_id")
    @ExcelProperty("视频活动id")
    private String videoActivityId;

    @Schema(description = "视频id")
    @TableField("video_id")
    @ExcelProperty("视频id")
    private String videoId;

    @Schema(description = "排序")
    @TableField("seq")
    @ExcelProperty("排序")
    private Integer seq;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    @ExcelProperty("奖励积分数")
    private BigDecimal rewardAmount;

    @Schema(description = "观看最小时长")
    @TableField("min_time")
    @ExcelProperty("观看最小时长")
    private Integer minTime;

    @Schema(description = "视频名称")
    @TableField(exist = false)
    private String videoTitle;

    @Schema(description = "视频封面图")
    @TableField(exist = false)
    private String videoUrlPic;

    @Schema(description = "视频封面图")
    @TableField(exist = false)
    private String videoUrlVideo;
}
