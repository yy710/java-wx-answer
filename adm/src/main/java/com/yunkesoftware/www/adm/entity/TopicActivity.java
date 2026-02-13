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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 答题活动
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_activity")
@Schema(name = "TopicActivity对象", description = "答题活动")
public class TopicActivity extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "每日答题线路限制")
    @TableField("limit_num")
    @ExcelProperty("每日答题线路限制")
    private Integer limitNum;

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

    @Schema(description = "参与人数")
    @TableField("user_num")
    @ExcelProperty("参与人数")
    private Integer userNum;

    @Schema(description = "全部点亮额外奖励积分数")
    @TableField("reward_extra")
    @ExcelProperty("全部点亮额外奖励积分数")
    private BigDecimal rewardExtra;

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
