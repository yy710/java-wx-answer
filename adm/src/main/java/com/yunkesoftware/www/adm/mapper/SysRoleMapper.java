package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.adm.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("select r.* from sys_role_user ru inner join sys_role r on r.id = ru.role_id where ru.user_id = #{userId}")
    Set<SysRole> findRolesByUserId(@Param("userId") String userId);
}
