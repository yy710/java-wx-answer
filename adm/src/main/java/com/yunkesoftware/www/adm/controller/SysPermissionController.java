package com.yunkesoftware.www.adm.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.adm.entity.SysPermission;
import com.yunkesoftware.www.adm.service.SysPermissionService;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.utils.QueryTreeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统权限")
@RestController
@RequestMapping("/sys/sysPermission")
public class SysPermissionController {

    @Resource
    private SysPermissionService sysPermissionService;

    @Operation(summary = "权限树")
    @GetMapping("/list")
    public CommonResult<JSONArray> permissionTree() {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSort);
        List<SysPermission> sysPermissionList = sysPermissionService.list(queryWrapper);

        JSONArray treeList = QueryTreeUtils.queryTree(sysPermissionList, SysPermission::getPid, SysPermission::getId, SysPermission::getSort, (item, json) -> json, "0");
        return CommonResult.success(treeList);
    }

    @Operation(summary = "父id查询下一级权限列表")
    @GetMapping("/lazyList")
    public CommonResult<List<SysPermission>> listByPid(String pid) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPid, pid)
                .orderByAsc(SysPermission::getSort);
        List<SysPermission> childrenList = sysPermissionService.list(queryWrapper);
        return CommonResult.success(childrenList);
    }

    @Operation(summary = "根据用户id查询权限列表")
    @GetMapping("/Treelist")
    public CommonResult<JSONArray> permissionTreeByUserId() {
        List<SysPermission> sysPermissionList = sysPermissionService.listByUserId(StpUtil.getLoginIdAsString());
        JSONArray treeList = QueryTreeUtils.queryTree(sysPermissionList, SysPermission::getPid, SysPermission::getId, SysPermission::getSort, (item, json) -> json, "0");
        return CommonResult.success(treeList);
    }

    @Operation(summary = "查询权限列表根据父id")
    @GetMapping("/listByPid")
    public CommonResult<JSONArray> permissionTreeByPid(@RequestParam("pid") String pid) {
        List<SysPermission> sysPermissionList = sysPermissionService.permissionTreeByPid(pid);
        JSONArray treeList = QueryTreeUtils.queryTree(sysPermissionList, SysPermission::getPid, SysPermission::getId, SysPermission::getSort, (item, json) -> json, "0");
        return CommonResult.success(treeList);
    }

    @Operation(summary = "新增或修改权限")
    @PostMapping("/savaOrupdate")
    public CommonResult<Object> addOrModify(@RequestBody SysPermission sysPermission) {
        sysPermissionService.saveOrUpdate(sysPermission);
        return CommonResult.success();
    }

    @Operation(summary = "删除权限标识")
    @PostMapping("/delete")
    public CommonResult<Object> delete(@RequestParam("id") String id) {
        sysPermissionService.recursionDelete(id);
        return CommonResult.success();
    }

    @PostMapping("/batchsavaOrupdate")
    public CommonResult<Object> addOrModifyBatch(@RequestBody List<SysPermission> sysPermissionList) {
        sysPermissionService.saveOrUpdateBatch(sysPermissionList);
        return CommonResult.success();
    }
}
