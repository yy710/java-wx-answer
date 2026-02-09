package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.SysUser;
import com.yunkesoftware.www.adm.query.SysUserQuery;
import com.yunkesoftware.www.adm.vo.SysUserVo;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 后台用户表 服务类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-27
 */
public interface SysUserService extends IService<SysUser> {

    Map<String, Object> login(SysUserVo sysUserVo);

    void add(SysUser sysUser);

    void modify(SysUser sysUser);

    void delete(List<String> ids);

    Page<SysUserVo> pageWithRole(SysUserQuery query);
}
