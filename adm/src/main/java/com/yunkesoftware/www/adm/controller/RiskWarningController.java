package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.RiskWarningService;
import com.yunkesoftware.www.adm.entity.RiskWarning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "风险提示")
@RestController
@RequestMapping("/sys/riskWarning")
public class RiskWarningController {
    @Resource
    private RiskWarningService riskWarningService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<RiskWarning>> page(@RequestBody RiskWarning riskWarning) {
        Page<RiskWarning> pageResult = riskWarningService.pageByQuery(riskWarning);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<RiskWarning> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(riskWarningService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    @YunkeSysLog("风险提示-添加或修改")
    public CommonResult<Boolean> addOrModify(@RequestBody RiskWarning riskWarning) {
        return CommonResult.status(riskWarningService.saveOrUpdate(riskWarning));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    @YunkeSysLog("风险提示-删除")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(riskWarningService.removeByIds(ids));
    }
}
