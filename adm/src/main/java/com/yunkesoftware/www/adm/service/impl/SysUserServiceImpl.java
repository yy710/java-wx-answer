package com.yunkesoftware.www.adm.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.entity.SysUser;
import com.yunkesoftware.www.adm.entity.SysUserRole;
import com.yunkesoftware.www.adm.mapper.SysUserMapper;
import com.yunkesoftware.www.adm.mapper.SysUserRoleMapper;
import com.yunkesoftware.www.adm.query.SysUserQuery;
import com.yunkesoftware.www.adm.service.SysUserService;
import com.yunkesoftware.www.adm.vo.SysUserVo;
import com.yunkesoftware.www.adm.vo.TokenInfoVo;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-27
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;


    @Override
    public Map<String, Object> login(SysUserVo sysUserVo) {
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, sysUserVo.getUsername()));

        if (sysUser == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "账号不存在");
        }

        String encryptPwd = SecureUtil.md5(sysUserVo.getPassword());
        if (!encryptPwd.equals(sysUser.getPassword())) {
            throw new YunKeException(ExceptionEnum.FAIL, "密码错误");
        }

        StpUtil.login(sysUser.getId());
        // 登陆成功生成token
        Map<String, Object> loginMap = new HashMap<>();
        sysUser.setPassword(null);
        TokenInfoVo tokenInfoVo = new TokenInfoVo();
        tokenInfoVo.setTokenName(StpUtil.getTokenName());
        tokenInfoVo.setTokenValue(StpUtil.getTokenValue());
        loginMap.put("userInfo", sysUser);
        loginMap.put("token", tokenInfoVo);
        return loginMap;
    }

    @Override
    public void add(SysUser sysUser) {
        SysUser checkData = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, sysUser.getUsername()));
        if (checkData != null) {
            throw new YunKeException(ExceptionEnum.FAIL, "该账号已存在");
        }
        sysUser.setPassword(SecureUtil.md5(sysUser.getPassword()));
        save(sysUser);
    }

    @Override
    public void modify(SysUser sysUser) {
        SysUser checkData = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, sysUser.getUsername())
                .ne(SysUser::getId, sysUser.getId()));
        if (checkData != null) {
            throw new YunKeException(ExceptionEnum.FAIL, "该账号已存在");
        }
        sysUser.setPassword(null);
        updateById(sysUser);
    }

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, ids));
    }

    @Override
    public Page<SysUserVo> pageWithRole(SysUserQuery query) {
        Page<SysUserVo> pageParam = new Page<>(query.getPageNum(), query.getPageSize());

        return baseMapper.pageWithRole(pageParam, query);
    }
}
