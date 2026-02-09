package com.yunkesoftware.www.web.entity;

import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
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
@TableName("ticket_video")
@Schema(name = "TicketVideo对象", description = "视频信息")
public class TicketVideo extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "活动id")
    @TableField("activity_id")
    private String activityId;

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

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "票数")
    @TableField("ticket_total")
    private Integer ticketTotal;

    @Schema(description = "最小观看时长")
    @TableField("min_time")
    private Integer minTime;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "是否获得积分")
    @TableField(exist = false)
    private boolean rewardFlag;
}
