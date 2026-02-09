package com.yunkesoftware.www.web.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.query.UserWalletRecordQuery;
import com.yunkesoftware.www.web.service.UserWalletRecordService;
import com.yunkesoftware.www.web.entity.UserWalletRecord;
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
@RequestMapping("/wx/userWalletRecord")
public class UserWalletRecordController {
    @Resource
    private UserWalletRecordService userWalletRecordService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult<Page<UserWalletRecord>> page(@RequestBody UserWalletRecordQuery query) {
        Page<UserWalletRecord> pageResult = userWalletRecordService.pageByQuery(query);
        return CommonResult.success(pageResult);
    }

}
