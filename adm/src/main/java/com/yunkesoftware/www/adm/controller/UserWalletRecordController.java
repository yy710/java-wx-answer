package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.UserWalletRecordService;
import com.yunkesoftware.www.adm.entity.UserWalletRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "用户钱包记录")
@RestController
@RequestMapping("/sys/userWalletRecord")
public class UserWalletRecordController {
    @Resource
    private UserWalletRecordService userWalletRecordService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<UserWalletRecord>> page(@RequestBody UserWalletRecord userWalletRecord) {
        Page<UserWalletRecord> pageResult = userWalletRecordService.pageByQuery(userWalletRecord);
        return CommonResult.success(pageResult);
    }

}
