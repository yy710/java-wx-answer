package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TicketRecordService;
import com.yunkesoftware.www.adm.entity.TicketRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "投票活动-记录")
@RestController
@RequestMapping("/sys/ticketRecord")
public class TicketRecordController {
    @Resource
    private TicketRecordService ticketRecordService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TicketRecord>> page(@RequestBody TicketRecord ticketRecord) {
        Page<TicketRecord> pageResult = ticketRecordService.pageByQuery(ticketRecord);
        return CommonResult.success(pageResult);
    }

}
