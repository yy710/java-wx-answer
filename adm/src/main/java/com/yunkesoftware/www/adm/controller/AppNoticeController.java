package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.AppNotice;
import com.yunkesoftware.www.adm.service.AppNoticeService;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author g
 * @since 2023-09-07
 */
@Tag(name = "app 首页公告")
@RestController
@RequestMapping("/sys/appNotice")
public class AppNoticeController {

    @Resource
    private AppNoticeService appNoticeService;

    @Operation(summary = "获取全部列表")
    @PostMapping("/list")
    public CommonResult<List<AppNotice>> list(@RequestBody AppNotice appNotice) {
        return CommonResult.success(appNoticeService.list(new LambdaQueryWrapper<>(appNotice)));
    }

    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult<Page<AppNotice>> page(@RequestBody AppNotice appNotice) {
        return CommonResult.success(appNoticeService.page(new Page<>(appNotice.getPageNum(), appNotice.getPageSize()),
                new LambdaQueryWrapper<AppNotice>()
                        .eq(appNotice.getType() != null, AppNotice::getType, appNotice.getType())
                        .orderByAsc(AppNotice::getSort)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<AppNotice> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(appNoticeService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody AppNotice appNotice) {
        return CommonResult.status(appNoticeService.saveOrUpdate(appNotice));
    }

    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(appNoticeService.removeByIds(ids));
    }
}
