package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.SysPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 后台用户权限表 服务类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
public interface SysPermissionService extends IService<SysPermission> {
    List<SysPermission> listByUserId(String userId);

    List<SysPermission> permissionTreeByPid(String pid);

    void recursionDelete(String id);

    List<SysPermission> listByRoleId(String roleId);
}
