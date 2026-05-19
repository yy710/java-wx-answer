package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.IntroduceService;
import com.yunkesoftware.www.web.entity.Introduce;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-03-11
 */
@Tag(name = "介绍信息")
@RestController
@RequestMapping("/wx/introduce")
public class IntroduceController {
    @Resource
    private IntroduceService introduceService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult<Page<Introduce>> page(@RequestBody Introduce introduce) {
        Page<Introduce> pageResult = introduceService.pageByQuery(introduce);

        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Introduce> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(introduceService.getById(id));
    }
}
