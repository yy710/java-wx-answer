package com.yunkesoftware.www.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.query.ScanPayQuery;
import com.yunkesoftware.www.web.service.UserWalletService;
import com.yunkesoftware.www.web.entity.UserWallet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "用户钱包")
@RestController
@RequestMapping("/wx/userWallet")
public class UserWalletController {
    @Resource
    private UserWalletService userWalletService;

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneByType")
    public CommonResult<UserWallet> getOneByType(@RequestParam(value = "type", defaultValue = "1") Integer type) {
        return CommonResult.success(userWalletService.getOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, StpUtil.getLoginIdAsString())
                .eq(UserWallet::getType, type)));
    }

    @Operation(summary = "扫码支付")
    @PostMapping("/scanPay")
    public CommonResult<Object> scanPay(@Valid @RequestBody ScanPayQuery query) {
        userWalletService.scanPay(query);
        return CommonResult.success();
    }
}
