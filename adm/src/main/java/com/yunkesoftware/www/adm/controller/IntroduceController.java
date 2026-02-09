package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.IntroduceService;
import com.yunkesoftware.www.adm.entity.Introduce;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2025-11-07
 */
@Tag(name = "介绍信息")
@RestController
@RequestMapping("/sys/introduce")
public class IntroduceController {
    @Resource
    private IntroduceService introduceService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<Introduce>> page(@RequestBody Introduce introduce) {
        return CommonResult.success(introduceService.page(new Page<>(introduce.getPageNum(), introduce.getPageSize()),
                new LambdaQueryWrapper<Introduce>()
                        .eq(introduce.getStatus() != null, Introduce::getStatus, introduce.getStatus())
                        .like(StringUtils.hasLength(introduce.getTitle()), Introduce::getTitle, introduce.getTitle())
                        .orderByAsc(Introduce::getSeq)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Introduce> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(introduceService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody Introduce introduce) {
        return CommonResult.status(introduceService.saveOrUpdate(introduce));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(introduceService.removeByIds(ids));
    }
}
