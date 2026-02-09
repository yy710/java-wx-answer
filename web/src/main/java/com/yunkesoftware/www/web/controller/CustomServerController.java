package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.entity.CustomServer;
import com.yunkesoftware.www.web.service.CustomServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author cuiyq
 * @since 2024-11-15
 */
@Tag(name = "客户服务")
@RestController
@RequestMapping("/wx/customServer")
public class CustomServerController {
    @Resource
    private CustomServerService customServerService;

    @Operation(summary = "列表")
    @PostMapping("/list")
    public CommonResult<List<CustomServer>> list(@RequestBody CustomServer customServer) {
        List<CustomServer> resultList = customServerService.list(new LambdaQueryWrapper<>(customServer));
        return CommonResult.success(resultList);
    }

}
