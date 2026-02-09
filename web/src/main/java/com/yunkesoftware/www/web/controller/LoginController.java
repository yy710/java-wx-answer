package com.yunkesoftware.www.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.yunkesoftware.www.result.CommonResult;

import com.yunkesoftware.www.web.service.*;
import com.yunkesoftware.www.web.vo.LoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "登录接口")
@RestController
@RequestMapping("/wx/login")
public class LoginController {
    @Resource
    private UserService userService;

    @Operation(summary = "微信授权登录")
    @PostMapping("/wxAuth")
    public CommonResult<Map<String, Object>> wxAuth(@RequestBody LoginVo loginVo) {
        Map<String, Object> resultMap = userService.wxAuth(loginVo);
        return CommonResult.success(resultMap);
    }


    @Operation(summary = "退出登录")
    @GetMapping("/logout")
    public CommonResult<Object> logout() {
        StpUtil.logout();
//        StpUtil.logoutByTokenValue(token);
        return CommonResult.success("退出成功");
    }


}
