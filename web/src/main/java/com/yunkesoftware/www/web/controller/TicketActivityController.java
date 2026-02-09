package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.TicketActivityService;
import com.yunkesoftware.www.web.entity.TicketActivity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-15
 */
@Tag(name = "投票活动")
@RestController
@RequestMapping("/wx/ticketActivity")
public class TicketActivityController {
    @Resource
    private TicketActivityService ticketActivityService;


    @Operation(summary = "获取开启的投票活动")
    @GetMapping("/getOpen")
    public CommonResult<TicketActivity> getOpen() {
        TicketActivity ticketActivity = ticketActivityService.getOpen();
        return CommonResult.success(ticketActivity);
    }

    @Operation(summary = "id查询")
    @GetMapping("/getOneById")
    public CommonResult<TicketActivity> getOneById(String id) {
        TicketActivity ticketActivity = ticketActivityService.getOne(new LambdaQueryWrapper<TicketActivity>()
                .eq(TicketActivity::getId, id)
                .select(TicketActivity::getId, TicketActivity::getDescr, TicketActivity::getTitle));
        return CommonResult.success(ticketActivity);
    }

    @Operation(summary = "获取用户剩余票数")
    @GetMapping("/surplusTicket")
    public CommonResult<Integer> surplusTicket(@RequestParam(value = "activityId") String activityId) {
        Integer ticket = ticketActivityService.surplusTicket(activityId);
        return CommonResult.success(ticket);
    }
}
