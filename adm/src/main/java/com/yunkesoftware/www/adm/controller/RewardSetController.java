package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.RewardSetService;
import com.yunkesoftware.www.adm.entity.RewardSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-31
 */
@Tag(name = "奖励设置")
@RestController
@RequestMapping("/sys/rewardSet")
public class RewardSetController {
    @Resource
    private RewardSetService rewardSetService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<RewardSet>> page(@RequestBody RewardSet rewardSet) {
        return CommonResult.success(rewardSetService.page(new Page<>(rewardSet.getPageNum(), rewardSet.getPageSize()),
                new LambdaQueryWrapper<RewardSet>()
                        .eq(RewardSet::getType, rewardSet.getType())));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<RewardSet> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(rewardSetService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    @YunkeSysLog(value = "奖励设置-添加或修改")
    public CommonResult<Boolean> addOrModify(@RequestBody RewardSet rewardSet) {
        return CommonResult.status(rewardSetService.saveOrUpdate(rewardSet));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    @YunkeSysLog(value = "奖励设置-删除")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(rewardSetService.removeByIds(ids));
    }
}
