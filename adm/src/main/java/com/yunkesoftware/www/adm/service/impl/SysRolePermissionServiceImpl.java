package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.entity.SysRolePermission;
import com.yunkesoftware.www.adm.mapper.SysRolePermissionMapper;
import com.yunkesoftware.www.adm.service.SysRolePermissionService;
import com.yunkesoftware.www.adm.vo.SysRolePermissionVo;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色权限关系表 服务实现类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {

    @Override
    public void doRoleAssign(SysRolePermissionVo rolePermissionVo) {
        baseMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, rolePermissionVo.getId()));
        SysRolePermission sysRolePermission = new SysRolePermission();
        sysRolePermission.setRoleId(rolePermissionVo.getId());
        for (String permissionId : rolePermissionVo.getPermissionIds()) {
            sysRolePermission.setPermissionId(permissionId);
            baseMapper.insert(sysRolePermission);
        }
    }
}
