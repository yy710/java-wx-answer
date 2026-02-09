package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.SignActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


/**
 * @author yk
 * @since 2026-01-31
 */
@Tag(name = "签到活动")
@RestController
@RequestMapping("/wx/signActivity")
public class SignActivityController {
    @Resource
    private SignActivityService signActivityService;

    @Operation(summary = "日常签到")
    @GetMapping("/dailySign")
    public CommonResult<BigDecimal> dailySign() {
        BigDecimal rewardAmount = signActivityService.dailySign();
        return CommonResult.success(rewardAmount);
    }
}
