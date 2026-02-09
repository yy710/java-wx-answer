package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yunkesoftware.www.adm.entity.SysRole;
import com.yunkesoftware.www.adm.entity.SysRolePermission;
import com.yunkesoftware.www.adm.mapper.SysRoleMapper;
import com.yunkesoftware.www.adm.mapper.SysRolePermissionMapper;
import com.yunkesoftware.www.adm.service.SysRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Resource
    private SysRolePermissionMapper sysRolePermissionMapper;


    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, ids));
    }
}
