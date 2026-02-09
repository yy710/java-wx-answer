package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 角色权限关系表
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SysRolePermission对象", description = "角色权限关系表")
public class SysRolePermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description ="角色id")
    @TableField("role_id")
    private String roleId;

    @Schema(description ="权限id")
    @TableField("permission_id")
    private String permissionId;


}
