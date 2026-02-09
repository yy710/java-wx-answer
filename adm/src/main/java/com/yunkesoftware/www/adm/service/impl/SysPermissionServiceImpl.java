package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.entity.SysPermission;
import com.yunkesoftware.www.adm.entity.SysRolePermission;
import com.yunkesoftware.www.adm.mapper.SysPermissionMapper;
import com.yunkesoftware.www.adm.mapper.SysRolePermissionMapper;
import com.yunkesoftware.www.adm.service.SysPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 后台用户权限表 服务实现类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Resource
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public List<SysPermission> listByUserId(String userId) {
        if ("1".equals(userId)) {
            return baseMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getStatus, 1));
        }
        // 去重
        List<SysPermission> resultList = new ArrayList<>();
        List<SysPermission> sysPermissionList = baseMapper.listByUserId(userId);
        for (SysPermission sysPermission : sysPermissionList) {
            boolean addFlag = true;
            for (SysPermission permission : resultList) {
                if (permission.getId().equals(sysPermission.getId())) {
                    addFlag = false;
                    break;
                }
            }
            if (addFlag) {
                resultList.add(sysPermission);
            }
        }
        return resultList;
    }

    @Override
    public List<SysPermission> permissionTreeByPid(String pid) {
        // 一级
        List<SysPermission> permissionList = baseMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPid, pid).orderByAsc(SysPermission::getSort));

        List<String> nextIds = permissionList.stream().map(SysPermission::getId).toList();

        while (nextIds.size() > 0) {
            List<SysPermission> nextPermissionList = baseMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                    .in(SysPermission::getId, nextIds).orderByAsc(SysPermission::getSort));
            if (nextPermissionList != null && nextPermissionList.size() > 0) {
                nextIds = nextPermissionList.stream().map(SysPermission::getId).toList();
                permissionList.addAll(nextPermissionList);
            } else {
                break;
            }
        }

        return permissionList;
    }

    @Override
    public void recursionDelete(String id) {
        baseMapper.deleteById(id);
        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id));
        // 递归调用删除所有下级菜单
        recursionDeleteChild(id);
    }

    @Override
    public List<SysPermission> listByRoleId(String roleId) {
        return baseMapper.listByRoleId(roleId);
    }

    private void recursionDeleteChild(String id) {
        List<SysPermission> list = baseMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPid, id));
        if (list != null && list.size() > 0) {
            for (SysPermission sysPermission : list) {
                baseMapper.deleteById(sysPermission.getId());
                sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getPermissionId, sysPermission.getId()));
                recursionDeleteChild(sysPermission.getId());
            }
        }
    }
}
