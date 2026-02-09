package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yunkesoftware.www.query.PageCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

;

/**
 * <p>
 * 地区管理
 * </p>
 *
 * @author g
 * @since 2023-12-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("yk_area")
@Schema(name = "Area对象", description = "地区管理")
public class Area extends PageCurrency {

    @TableId(value = "area_id", type = IdType.AUTO)
    private Long areaId;

    @TableField("area_name")
    private String areaName;

    @TableField("parent_id")
    private Long parentId;

    @TableField("level")
    private Integer level;

    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Area> children;
}
