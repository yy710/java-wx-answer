package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 分类信息
 * </p>
 *
 * @author yk
 * @since 2026-02-02
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("category")
@Schema(name = "Category对象", description = "分类信息")
public class Category {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "图片")
    @TableField("pic")
    private String pic;

    @Schema(description = "简介")
    @TableField("descr")
    private String descr;

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "状态")
    @TableField("status")
    private Boolean status;
}
