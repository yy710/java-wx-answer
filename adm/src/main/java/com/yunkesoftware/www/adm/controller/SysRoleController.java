package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.SysPermission;
import com.yunkesoftware.www.adm.entity.SysRole;
import com.yunkesoftware.www.adm.query.SysRoleQuery;
import com.yunkesoftware.www.adm.service.SysPermissionService;
import com.yunkesoftware.www.adm.service.SysRolePermissionService;
import com.yunkesoftware.www.adm.service.SysRoleService;
import com.yunkesoftware.www.adm.vo.SysRolePermissionVo;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统用户")
@RestController
@RequestMapping("/sys/sysRole")
public class SysRoleController {
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysPermissionService sysPermissionService;
    @Resource
    private SysRolePermissionService sysRolePermissionService;

    @Operation(summary = "分页数据")
    @PostMapping("/page")
    public CommonResult<Page<SysRole>> page(@RequestBody SysRoleQuery query) {
        Page<SysRole> pageParam = new Page<>(query.getPageNum(), query.getPageSize());
        Page<SysRole> sysRolePage = sysRoleService.page(pageParam);
        return CommonResult.success(sysRolePage);
    }

    @Operation(summary = "所有数据")
    @GetMapping("/listAll")
    public CommonResult<List<SysRole>> listAll() {
        List<SysRole> sysRoleList = sysRoleService.list();
        return CommonResult.success(sysRoleList);
    }

    @Operation(summary = "新增或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Object> addOrModify(@RequestBody SysRole sysRole) {
        sysRoleService.saveOrUpdate(sysRole);
        return CommonResult.success();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    public CommonResult<Object> delete(@RequestBody List<String> ids) {
        sysRoleService.delete(ids);
        return CommonResult.success();
    }

    @Operation(summary = "角色id查询权限集合")
    @GetMapping(value = "/listByRoleId")
    public CommonResult<List<SysPermission>> listByRoleId(@RequestParam("id") String roleId) {
        List<SysPermission> permissionSet = sysPermissionService.listByRoleId(roleId);
        return CommonResult.success(permissionSet);
    }

    @Operation(summary = "角色设置权限")
    @PostMapping(value = "/setPermission")
    public CommonResult<String> setPermission(@RequestBody SysRolePermissionVo rolePermissionVo) {
        sysRolePermissionService.doRoleAssign(rolePermissionVo);
        return CommonResult.success();
    }
}
