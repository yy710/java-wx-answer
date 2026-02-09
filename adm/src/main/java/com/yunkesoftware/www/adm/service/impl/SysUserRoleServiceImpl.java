package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.entity.SysUserRole;
import com.yunkesoftware.www.adm.mapper.SysUserRoleMapper;
import com.yunkesoftware.www.adm.service.SysUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
    @Override
    public void setRole(SysUserRole sysUserRole) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, sysUserRole.getUserId())
                .eq(SysUserRole::getRoleId, sysUserRole.getRoleId()));
        if (count == null || count == 0) {
            baseMapper.insert(sysUserRole);
        }
    }
}
