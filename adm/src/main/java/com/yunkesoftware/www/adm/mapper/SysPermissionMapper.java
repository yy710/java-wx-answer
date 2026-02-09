package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.adm.entity.SysPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 后台用户权限表 Mapper 接口
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
@InterceptorIgnore(tenantLine = "true")
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    List<SysPermission> listByRoleId(@Param("roleId") String roleId);

    List<SysPermission> listByUserId(@Param("userId") String userId);

}
