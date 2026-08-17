package com.yunkesoftware.www.web.entity;

import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


/**
 * <p>
 * 视频信息
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("video")
@Schema(name = "Video对象", description = "视频信息")
public class Video extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "文字介绍")
    @TableField("descr")
    private String descr;

    @Schema(description = "是否开启")
    @TableField("status")
    private Boolean status;

    @Schema(description = "视频链接")
    @TableField("url_video")
    private String urlVideo;

    @Schema(description = "封面图")
    @TableField("url_pic")
    private String urlPic;

    @Schema(description = "是否作为每日消保视频候选")
    @TableField("daily_task_enabled")
    private Boolean dailyTaskEnabled;

    @Schema(description = "可信视频时长（秒）")
    @TableField("duration_seconds")
    private Integer durationSeconds;


    @Schema(description = "票数")
    @TableField("ticket_total")
    private Integer ticketTotal;

    @Schema(description = "是否已获得积分")
    @TableField(exist = false)
    private boolean getFlag;


    @Schema(description = "奖励积分数")
    @TableField(exist = false)
    private BigDecimal rewardAmount;

    @Schema(description = "观看最小时长")
    @TableField(exist = false)
    private Integer minTime;

    @Schema(description = "当前用户投票数")
    @TableField(exist = false)
    private long ticketNum;

    @Schema(description = "活动id")
    @TableField(exist = false)
    private String activityId;
}
