package com.yunkesoftware.www.adm.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.yunkesoftware.www.adm.entity.DailyTaskConfig;
import com.yunkesoftware.www.adm.service.DailyTaskConfigService;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "每日任务设置")
@RestController
@RequestMapping("/sys/dailyTaskConfig")
public class DailyTaskConfigController {
    @Resource private DailyTaskConfigService service;

    @Operation(summary = "读取每日任务单例配置")
    @GetMapping("/get")
    public CommonResult<DailyTaskConfig> get() { return CommonResult.success(service.getById(1)); }

    @Operation(summary = "乐观锁更新每日任务配置")
    @PostMapping("/update")
    @YunkeSysLog("每日任务配置更新")
    public CommonResult<DailyTaskConfig> update(@RequestBody DailyTaskConfig config) {
        return CommonResult.success(service.updateWithVersion(config, StpUtil.getLoginIdAsString()));
    }
}
