package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.RiskWarningRecordService;
import com.yunkesoftware.www.adm.entity.RiskWarningRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author yk
 * @since 2026-01-31
 */
@Tag(name = "风险阅读记录")
@RestController
@RequestMapping("/sys/riskWarningRecord")
public class RiskWarningRecordController {
    @Resource
    private RiskWarningRecordService riskWarningRecordService;

    @Operation(summary ="分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<RiskWarningRecord>> page(@RequestBody RiskWarningRecord riskWarningRecord) {
        Page<RiskWarningRecord> pageResult = riskWarningRecordService.pageByQuery(riskWarningRecord);
        return CommonResult.success(pageResult);
    }


}
