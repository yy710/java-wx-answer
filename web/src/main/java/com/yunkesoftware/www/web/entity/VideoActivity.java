package com.yunkesoftware.www.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频积分活动
 * </p>
 *
 * @author yk
 * @since 2026-02-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("video_activity")
@Schema(name = "VideoActivity对象", description = "视频积分活动")
public class VideoActivity {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "奖励限制数量")
    @TableField("reward_limit")
    private Integer rewardLimit;

    @Schema(description = "视频活动规则说明")
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
}
