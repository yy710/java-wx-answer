package com.yunkesoftware.www.adm.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.SysUser;
import com.yunkesoftware.www.adm.entity.SysUserRole;
import com.yunkesoftware.www.adm.query.SysUserQuery;
import com.yunkesoftware.www.adm.service.SysUserRoleService;
import com.yunkesoftware.www.adm.service.SysUserService;
import com.yunkesoftware.www.adm.vo.SysUserVo;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统用户")
@RestController
@RequestMapping("/sys/sysUser")
public class SysUserController {
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysUserRoleService sysUserRoleService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody SysUserVo sysUserVo) {
        Map<String, Object> resultMap = sysUserService.login(sysUserVo);
        return CommonResult.success(resultMap);
    }

    @Operation(summary = "退出登录")
    @GetMapping("/logout")
    public CommonResult<Object> logout(String token) {
//        sysUserService.logout(token);
        return CommonResult.success();
    }

    @Operation(summary = "新增")
    @PostMapping("/add")
    public CommonResult<String> add(@RequestBody SysUser sysUser) {
        sysUserService.add(sysUser);
        return CommonResult.success();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/updatePassword")
    public CommonResult<Object> updatePwd(@RequestBody SysUser sysUser) {
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, sysUser.getId())
                .set(SysUser::getPassword, SecureUtil.md5(sysUser.getPassword()));
        sysUserService.update(updateWrapper);
        return CommonResult.success();
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/getOneById")
    public CommonResult<SysUser> getOneById(@RequestParam("id") String id) {
        SysUser sysUser = sysUserService.getById(id);
        return CommonResult.success(sysUser);
    }

    @Operation(summary = "修改")
    @PostMapping("/modify")
    public CommonResult<Object> modify(@RequestBody SysUser sysUser) {
        sysUserService.modify(sysUser);
        return CommonResult.success();
    }

    @Operation(summary = "查询所有用户")
    @GetMapping("/selectAll")
    public CommonResult<List<SysUser>> selectAll() {
        List<SysUser> sysUserList = sysUserService.list();
        return CommonResult.success(sysUserList);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    public CommonResult<Object> delete(@RequestBody List<String> ids) {
        sysUserService.delete(ids);
        return CommonResult.success();
    }

    @Operation(summary = "分页查询所有用户")
    @PostMapping("/page")
    public CommonResult<Page<SysUserVo>> page(@RequestBody SysUserQuery query) {
        Page<SysUserVo> pageResult = sysUserService.pageWithRole(query);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "设置角色")
    @PostMapping("/setRole")
    public CommonResult<String> setRole(@RequestBody SysUserRole sysUserRole) {
        sysUserRoleService.setRole(sysUserRole);
        return CommonResult.success();
    }

    @Operation(summary = "删除角色")
    @PostMapping("/deleteRole")
    public CommonResult<String> deleteRole(@RequestBody SysUserRole sysUserRole) {
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, sysUserRole.getRoleId())
                .eq(SysUserRole::getUserId, sysUserRole.getUserId()));
        return CommonResult.success();
    }
}
