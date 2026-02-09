package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.SignActivityService;
import com.yunkesoftware.www.adm.entity.SignActivity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-31
 */
@Tag(name = "签到活动")
@RestController
@RequestMapping("/sys/signActivity")
public class SignActivityController {
    @Resource
    private SignActivityService signActivityService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<SignActivity>> page(@RequestBody SignActivity signActivity) {
        return CommonResult.success(signActivityService.page(new Page<>(signActivity.getPageNum(), signActivity.getPageSize()),
                new LambdaQueryWrapper<SignActivity>()
                        .like(StringUtils.hasLength(signActivity.getTitle()), SignActivity::getTitle, signActivity.getTitle())
                        .orderByAsc(SignActivity::getEndTime)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<SignActivity> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(signActivityService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Object> addOrModify(@Valid @RequestBody SignActivity signActivity) {
        signActivityService.addOrModify(signActivity);
        return CommonResult.success();
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        signActivityService.delete(ids);
        return CommonResult.success();
    }
}
