package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.SysUser;
import com.yunkesoftware.www.adm.query.SysUserQuery;
import com.yunkesoftware.www.adm.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 后台用户表 Mapper 接口
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-27
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    Page<SysUserVo> pageWithRole(Page<SysUserVo> pageParam, @Param("param") SysUserQuery query);
}
