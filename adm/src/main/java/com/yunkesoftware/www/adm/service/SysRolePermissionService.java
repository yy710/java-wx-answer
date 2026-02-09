package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.SysRolePermission;
import com.yunkesoftware.www.adm.vo.SysRolePermissionVo;

/**
 * <p>
 * 角色权限关系表 服务类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
public interface SysRolePermissionService extends IService<SysRolePermission> {

    void doRoleAssign(SysRolePermissionVo rolePermissionVo);
}
