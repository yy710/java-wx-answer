package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.TicketVideoService;
import com.yunkesoftware.www.web.entity.TicketVideo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-15
 */
@Tag(name = "投票活动-视频")
@RestController
@RequestMapping("/wx/ticketVideo")
public class TicketVideoController {
    @Resource
    private TicketVideoService ticketVideoService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult<Page<TicketVideo>> page(@RequestBody TicketVideo ticketVideo) {
        Page<TicketVideo> pageResult = ticketVideoService.pageByQuery(ticketVideo);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TicketVideo> getOneById(@RequestParam("id") String id) {
        TicketVideo ticketVideo = ticketVideoService.getOneById(id);
        return CommonResult.success(ticketVideo);
    }

    @Operation(summary = "投票")
    @GetMapping("/doVote")
    public CommonResult<Object> doVote(@RequestParam("id") String id) {
        ticketVideoService.doVote(id);
        return CommonResult.success();
    }

    @Operation(summary = "获得积分奖励")
    @GetMapping("/doReward")
    public CommonResult<Object> doReward(@RequestParam("id") String id) {
        ticketVideoService.doReward(id);
        return CommonResult.success();
    }
}
