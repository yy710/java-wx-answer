package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * <p>
 * 后台用户权限表
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
@Data
@Schema(name = "SysPermission对象", description = "后台用户权限表")
public class SysPermission {

    @TableId("id")
    @Schema(description = "主键")
    private String id;

    @TableField("`pid`")
    @Schema(description = "父级权限id")
    private String pid;

    @TableField("`name`")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "权限值")
    private Integer permission;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "权限类型：0->目录；1->菜单；2->按钮（接口绑定权限）")
    private Integer type;

    @Schema(description = "前端资源路径")
    private String uri;

    @Schema(description = "排序")
    private Integer sort;

    @TableField("`code`")
    private String code;

    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

}
