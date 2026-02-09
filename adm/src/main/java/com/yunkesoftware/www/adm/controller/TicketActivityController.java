package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TicketActivityService;
import com.yunkesoftware.www.adm.entity.TicketActivity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "投票活动")
@RestController
@RequestMapping("/sys/ticketActivity")
public class TicketActivityController {
    @Resource
    private TicketActivityService ticketActivityService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TicketActivity>> page(@RequestBody TicketActivity ticketActivity) {
        return CommonResult.success(ticketActivityService.page(new Page<>(ticketActivity.getPageNum(), ticketActivity.getPageSize()),
                new LambdaQueryWrapper<TicketActivity>()
                        .like(StringUtils.hasLength(ticketActivity.getTitle()), TicketActivity::getTitle, ticketActivity.getTitle())));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TicketActivity> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(ticketActivityService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    @YunkeSysLog("投票活动-添加或修改")
    public CommonResult<Boolean> addOrModify(@Valid @RequestBody TicketActivity ticketActivity) {
        ticketActivityService.addOrModify(ticketActivity);
        return CommonResult.success();
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    @YunkeSysLog("投票活动-删除")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        ticketActivityService.delete(ids);
        return CommonResult.success();
    }
}
