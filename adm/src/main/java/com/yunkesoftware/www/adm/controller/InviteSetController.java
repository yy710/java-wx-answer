package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.InviteSetService;
import com.yunkesoftware.www.adm.entity.InviteSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-31
 */
@Tag(name = "拉新奖励设置")
@RestController
@RequestMapping("/sys/inviteSet")
public class InviteSetController {
    @Resource
    private InviteSetService inviteSetService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<InviteSet>> page(@RequestBody InviteSet inviteSet) {
        return CommonResult.success(inviteSetService.page(new Page<>(inviteSet.getPageNum(), inviteSet.getPageSize()),
                null));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<InviteSet> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(inviteSetService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    @YunkeSysLog(value = "拉新奖励设置-添加或修改")
    public CommonResult<Boolean> addOrModify(@RequestBody InviteSet inviteSet) {
        return CommonResult.status(inviteSetService.saveOrUpdate(inviteSet));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    @YunkeSysLog(value = "拉新奖励设置-删除")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(inviteSetService.removeByIds(ids));
    }
}
