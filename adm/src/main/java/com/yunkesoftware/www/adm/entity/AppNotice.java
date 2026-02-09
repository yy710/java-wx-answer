package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.utils.LocalDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * app 首页公告
 * </p>
 *
 * @author g
 * @since 2023-09-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("yk_app_notice")
@Schema(name = "AppNotice对象", description = "app 首页公告")
public class AppNotice extends PageCurrency {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @Schema(description = "公告内容")
    @TableField("note")
    private String note;

    @Schema(description = "图片")
    @TableField("pic")
    private String pic;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "简介 首屏滚动内容")
    @TableField("brief")
    private String brief;

    @Schema(description = "类型")
    @TableField("type")
    private Integer type;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime updateTime;

}
