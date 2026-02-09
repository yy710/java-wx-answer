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

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 风险提示
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("risk_warning")
@Schema(name = "RiskWarning对象", description = "风险提示")
public class RiskWarning extends PageCurrency {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    @ExcelProperty("奖励积分数")
    private BigDecimal rewardAmount;

    @Schema(description = "分类id")
    @TableField("category_id")
    @ExcelProperty("分类id")
    private String categoryId;

    @Schema(description = "是否开启")
    @TableField("status")
    @ExcelProperty("是否开启")
    private Boolean status;

    @Schema(description = "排序")
    @TableField("seq")
    @ExcelProperty("排序")
    private Integer seq;

    @Schema(description = "阅读人数")
    @TableField("view_num")
    @ExcelProperty("阅读人数")
    private Integer viewNum;

    @Schema(description = "获得奖励人数")
    @TableField("reward_num")
    @ExcelProperty("获得奖励人数")
    private Integer rewardNum;

    @Schema(description = "阅读时长最低几秒")
    @TableField("time_min")
    @ExcelProperty("阅读时长最低几秒")
    private Integer timeMin;

    @Schema(description = "介绍信息")
    @TableField("descr")
    @ExcelProperty("介绍信息")
    private String descr;

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

    @TableField(exist = false)
    @Schema(description = "分类名称")
    private String categoryTitle;
}
