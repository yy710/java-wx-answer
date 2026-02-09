package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    @TableField("user_id")
    private String userId;

    @TableField("role_id")
    private String roleId;

}
