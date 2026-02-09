package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yunkesoftware.www.adm.entity.User;
import com.yunkesoftware.www.adm.mapper.UserMapper;
import com.yunkesoftware.www.adm.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author cuiyq
 * @since 2025-02-13
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public Page<User> pageByQuery(User user) {
        Page<User> pageParam = new Page<>(user.getPageNum(), user.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasLength(user.getMobile()), User::getNickName, user.getMobile());
        return baseMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public User getOneById(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void modify(User user) {

    }
}
