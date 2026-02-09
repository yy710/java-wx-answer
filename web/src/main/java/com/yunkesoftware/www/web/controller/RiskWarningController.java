package com.yunkesoftware.www.web.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.RiskWarningService;
import com.yunkesoftware.www.web.entity.RiskWarning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "风险提示")
@RestController
@RequestMapping("/wx/riskWarning")
public class RiskWarningController {
    @Resource
    private RiskWarningService riskWarningService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult<Page<RiskWarning>> page(@RequestBody RiskWarning riskWarning) {
        return CommonResult.success(riskWarningService.page(new Page<>(riskWarning.getPageNum(), riskWarning.getPageSize()),
                new LambdaQueryWrapper<RiskWarning>()
                        .eq(RiskWarning::getStatus, true)
                        .eq(RiskWarning::getCategoryId, riskWarning.getCategoryId())
                        .orderByAsc(RiskWarning::getSeq)
                        .select(RiskWarning::getId, RiskWarning::getTitle, RiskWarning::getRewardAmount, RiskWarning::getCreateTime)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<RiskWarning> getOneById(@RequestParam("id") String id) {
        RiskWarning riskWarning = riskWarningService.getOneById(id);
        return CommonResult.success(riskWarning);
    }

    @Operation(summary = "阅读完成")
    @GetMapping("/readFinish")
    public CommonResult<BigDecimal> readFinish(@RequestParam("id") String id) {
        BigDecimal rewardAmount = riskWarningService.readFinish(id);
        return CommonResult.success(rewardAmount);
    }
}
