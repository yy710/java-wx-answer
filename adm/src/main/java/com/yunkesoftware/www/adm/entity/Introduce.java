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
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 介绍信息
 * </p>
 *
 * @author yk
 * @since 2025-11-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("yk_introduce")
@Schema(name = "Introduce对象", description = "介绍信息")
public class Introduce extends PageCurrency {

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

    @Schema(description = "头像")
    @TableField("pic")
    private String pic;

    @Schema(description = "详情")
    @TableField("descr")
    @ExcelProperty("详情")
    private String descr;

    @Schema(description = "浏览次数")
    @TableField("view_num")
    @ExcelProperty("浏览次数")
    private Integer viewNum;

    @Schema(description = "排序")
    @TableField("seq")
    @ExcelProperty("排序")
    private Integer seq;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "是否启用")
    @TableField("status")
    @ExcelProperty("是否启用")
    private Boolean status;
}
