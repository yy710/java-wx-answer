package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VideoActivity extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    @ExcelProperty("标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "奖励限制")
    @TableField("reward_limit")
    private Integer rewardLimit;

    @Schema(description = "视频活动规则说明")
    @TableField("descr")
    private String descr;

    @Schema(description = "开始时间")
    @TableField("start_time")
    @ExcelProperty("开始时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    @ExcelProperty("结束时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @ExcelProperty("修改时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime updateTime;
}
