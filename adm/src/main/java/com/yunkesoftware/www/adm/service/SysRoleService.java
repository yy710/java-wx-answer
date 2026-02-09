package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.SysPermission;
import com.yunkesoftware.www.adm.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
public interface SysRoleService extends IService<SysRole> {
    void delete(List<String> ids);
}
