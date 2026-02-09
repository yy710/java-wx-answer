package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 投票活动信息
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("ticket_activity")
@Schema(name = "TicketActivity对象", description = "投票活动信息")
public class TicketActivity extends PageCurrency {

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ExcelIgnore
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    @ExcelProperty("标题")
    @NotBlank(message = "标题不能为空")
    private String title;

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

    @Schema(description = "投票结束时间")
    @TableField("end_ticket_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    @NotNull(message = "投票结束时间不能为空")
    private LocalDateTime endTicketTime;

    @Schema(description = "投票开始时间")
    @TableField("start_ticket_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    @NotNull(message = "投票开始时间不能为空")
    private LocalDateTime startTicketTime;

    @Schema(description = "每个用户每天多少票")
    @TableField("ticket_limit")
    @ExcelProperty("每个用户每天多少票")
    @NotNull(message = "每个用户每天多少票不能为空")
    private Integer ticketLimit;

    @Schema(description = "投票人数")
    @TableField("ticket_user_num")
    @ExcelProperty("投票人数")
    private Integer ticketUserNum;

    @Schema(description = "总票数")
    @TableField("ticket_total")
    @ExcelProperty("总票数")
    private Integer ticketTotal;

    @Schema(description = "浏览量")
    @TableField("view_num")
    @ExcelProperty("浏览量")
    private Integer viewNum;

    @Schema(description = "规则说明")
    @TableField("descr")
    private String descr;

    @Schema(description = "投票人数显示倍数")
    @TableField("ticket_user_multiple")
    @ExcelProperty("投票人数显示倍数")
    private Integer ticketUserMultiple = 1;

    @Schema(description = "总票数显示倍数")
    @TableField("ticket_multiple")
    @ExcelProperty("总票数显示倍数")
    private Integer ticketMultiple = 1;

    @Schema(description = "浏览量显示倍数")
    @TableField("view_multiple")
    @ExcelProperty("浏览量显示倍数")
    private Integer viewMultiple = 1;

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
