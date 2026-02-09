package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.UserWalletService;
import com.yunkesoftware.www.adm.entity.UserWallet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "用户钱包")
@RestController
@RequestMapping("/sys/userWallet")
public class UserWalletController {
    @Resource
    private UserWalletService userWalletService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<UserWallet>> page(@RequestBody UserWallet userWallet) {
        Page<UserWallet> pageResult = userWalletService.pageByQuery(userWallet);
        return CommonResult.success(pageResult);
    }

}
