package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.CustomServer;
import com.yunkesoftware.www.adm.service.CustomServerService;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客服")
@RestController
@RequestMapping("/sys/customerServer")
public class CustomServerController {
    @Resource
    private CustomServerService customServerService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult<Page<CustomServer>> page(@RequestBody PageCurrency query) {
        Page<CustomServer> pageParam = new Page<>(query.getPageNum(), query.getPageSize());
        Page<CustomServer> pageResult = customServerService.page(pageParam);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "新增或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Object> addOrModify(@RequestBody CustomServer customServer) {
        customServerService.saveOrUpdate(customServer);
        return CommonResult.success();
    }

    @Operation(summary = "分页")
    @GetMapping("/getOneById")
    public CommonResult<CustomServer> getOneById(String id) {
        CustomServer customServer = customServerService.getById(id);
        return CommonResult.success(customServer);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    public CommonResult<Object> delete(@RequestBody List<String> ids) {
        customServerService.removeByIds(ids);
        return CommonResult.success();
    }
}
