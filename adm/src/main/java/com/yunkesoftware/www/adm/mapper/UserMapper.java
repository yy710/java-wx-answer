package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.adm.entity.User;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author cuiyq
 * @since 2025-02-13
 */
public interface UserMapper extends BaseMapper<User> {

    Long countByDate(@Param("dateParam") LocalDate nowDate);
}
