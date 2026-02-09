package com.yunkesoftware.www.adm.vo;

import com.yunkesoftware.www.adm.entity.SysRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class SysUserVo {
    @Schema(description = "用户id")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String icon;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "昵称")
    private String nickName;


    @Schema(description = "角色集合")
    private List<SysRole> roleVo;
}
