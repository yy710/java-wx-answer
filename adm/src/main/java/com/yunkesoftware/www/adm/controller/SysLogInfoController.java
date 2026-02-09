package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.SysLogInfo;
import com.yunkesoftware.www.adm.service.SysLogInfoService;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 系统日志 前端控制器
 * </p>
 *
 * @author guozhijie
 * @since 2021-08-27
 */
@RestController
@Tag(name = "系统日志信息")
@RequestMapping("/sys/sysLogInfo")
public class SysLogInfoController {
    @Resource
    private SysLogInfoService sysLogInfoService;

    @Operation(summary ="分页获取系统日志")
    @PostMapping("/page")
    private CommonResult page(@RequestBody SysLogInfo sysLogInfo) {
        return CommonResult.success(sysLogInfoService.page(new Page<>(sysLogInfo.getPageNum(), sysLogInfo.getPageSize()),
                new LambdaQueryWrapper<>(sysLogInfo).orderByDesc(SysLogInfo::getCreateTime)));

    }


    @PostMapping("/saveOrUpdata")
    @Operation(summary ="新增或修改")
    public CommonResult saveOrUpdate(@RequestBody SysLogInfo sysLogInfo) {
        return CommonResult.status(sysLogInfoService.saveOrUpdate(sysLogInfo));
    }


    @PostMapping("/list")
    @Operation(summary ="列表查询")
    public CommonResult list(@RequestBody SysLogInfo sysLogInfo) {
        List<SysLogInfo> list = sysLogInfoService.
                list(new LambdaQueryWrapper<>(sysLogInfo).orderByDesc(SysLogInfo::getCreateTime));
        return CommonResult.success(list);
    }

    @PostMapping("/details")
    @Operation(summary ="获取")
    public CommonResult details(@RequestBody SysLogInfo sysLogInfo) {
        SysLogInfo one = sysLogInfoService.getOne(new QueryWrapper<>(sysLogInfo));
        return CommonResult.success(one);
    }

    @PostMapping("/remove")
    @Operation(summary ="删除")
    public CommonResult remove(@RequestBody List<String> ids) {
        Boolean one = sysLogInfoService.removeByIds(ids);
        return CommonResult.status(one);
    }
}

