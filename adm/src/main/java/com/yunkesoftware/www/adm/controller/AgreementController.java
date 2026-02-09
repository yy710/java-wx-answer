package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.adm.entity.Agreement;
import com.yunkesoftware.www.adm.service.AgreementService;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author g
 * @since 2023-09-07
 */
@Tag(name = "协议")
@RestController
@RequestMapping("/sys/agreement")
public class AgreementController {
    @Resource
    private AgreementService agreementService;

    @Operation(summary = "获取全部列表")
    @PostMapping("/list")
    public CommonResult<List<Agreement>> list(@RequestBody Agreement agreement) {
        return CommonResult.success(agreementService.list(new LambdaQueryWrapper<>(agreement)));
    }

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<Agreement>> page(@RequestBody Agreement agreement) {
        return CommonResult.success(agreementService.page(
                new Page<>(agreement.getPageNum(), agreement.getPageSize()),
                new LambdaQueryWrapper<Agreement>()
                        .eq(Agreement::getType, agreement.getType())));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Agreement> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(agreementService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody Agreement agreement) {
        return CommonResult.status(agreementService.saveOrUpdate(agreement));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(agreementService.removeByIds(ids));
    }
}
