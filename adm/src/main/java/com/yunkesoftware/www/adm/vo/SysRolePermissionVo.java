package com.yunkesoftware.www.adm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class SysRolePermissionVo implements Serializable {
    @Schema(description ="角色id")
    private String id;

    @Schema(description ="权限id集合")
    private Set<String> permissionIds;
}
