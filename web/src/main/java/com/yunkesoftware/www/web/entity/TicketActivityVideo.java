package com.yunkesoftware.www.web.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 投票活动-视频信息
 * </p>
 *
 * @author yk
 * @since 2026-02-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("ticket_activity_video")
@Schema(name = "TicketActivityVideo对象", description = "投票活动-视频信息")
public class TicketActivityVideo {

    @Schema(description = "投票活动id")
    @TableField("ticket_activity_id")
    private String ticketActivityId;

    @Schema(description = "视频id")
    @TableField("video_id")
    private String videoId;

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "观看最小时长")
    @TableField("min_time")
    private Integer minTime;
}
