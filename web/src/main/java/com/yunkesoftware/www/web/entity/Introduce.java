package com.yunkesoftware.www.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
 * @since 2026-03-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("introduce")
@Schema(name = "Introduce对象", description = "介绍信息")
public class Introduce extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "作者")
    @TableField("author")
    private String author;

    @Schema(description = "详情")
    @TableField("descr")
    private String descr;


    @Schema(description = "是否启用")
    @TableField("status")
    private Boolean status;

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;
}
