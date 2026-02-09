package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TicketVideoService;
import com.yunkesoftware.www.adm.entity.TicketVideo;
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
@Tag(name = "投票活动-视频")
@RestController
@RequestMapping("/sys/ticketVideo")
public class TicketVideoController {
    @Resource
    private TicketVideoService ticketVideoService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TicketVideo>> page(@RequestBody TicketVideo ticketVideo) {
        return CommonResult.success(ticketVideoService.page(new Page<>(ticketVideo.getPageNum(), ticketVideo.getPageSize()),
                new LambdaQueryWrapper<TicketVideo>()
                        .eq(TicketVideo::getActivityId, ticketVideo.getActivityId())
                        .like(StringUtils.hasLength(ticketVideo.getTitle()), TicketVideo::getTitle, ticketVideo.getTitle())
                        .orderByAsc(TicketVideo::getSeq)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TicketVideo> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(ticketVideoService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    @YunkeSysLog("活动视频-添加或修改")
    public CommonResult<Object> addOrModify(@Valid @RequestBody TicketVideo ticketVideo) {
        ticketVideoService.saveOrUpdate(ticketVideo);
        return CommonResult.success();
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    @YunkeSysLog("活动视频-删除")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(ticketVideoService.removeByIds(ids));
    }

}
