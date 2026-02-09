package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.SysUserRole;

public interface SysUserRoleService extends IService<SysUserRole> {
    void setRole(SysUserRole sysUserRole);
}
