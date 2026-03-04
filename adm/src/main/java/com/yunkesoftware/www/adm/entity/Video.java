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
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 视频信息
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("video")
@Schema(name = "Video对象", description = "视频信息")
public class Video extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "文字介绍")
    @TableField("descr")
    @ExcelProperty("文字介绍")
    private String descr;

    @Schema(description = "是否开启")
    @TableField("status")
    @ExcelProperty("是否开启")
    private Boolean status;

    @Schema(description = "视频链接")
    @TableField("url_video")
    @ExcelProperty("视频链接")
    private String urlVideo;

    @Schema(description = "封面图")
    @TableField("url_pic")
    @ExcelProperty("封面图")
    private String urlPic;

    @Schema(description = "排序")
    @TableField("seq")
    @ExcelProperty("排序")
    private Integer seq;

    @Schema(description = "票数")
    @TableField("ticket_total")
    @ExcelProperty("票数")
    private Integer ticketTotal;

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
