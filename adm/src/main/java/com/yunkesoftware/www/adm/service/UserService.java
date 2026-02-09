package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.User;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author cuiyq
 * @since 2025-02-13
 */
public interface UserService extends IService<User> {

    Page<User> pageByQuery(User user);

    User getOneById(String id);

    void delete(List<String> ids);

    void modify(User user);
}
