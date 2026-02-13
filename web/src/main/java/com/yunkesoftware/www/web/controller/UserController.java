package com.yunkesoftware.www.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.vo.UserVo;
import com.yunkesoftware.www.web.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户信息")
@RestController
@RequestMapping("/wx/user")
public class UserController {
    @Resource
    private UserService userService;

    @Operation(summary = "当前登录的用户信息")
    @GetMapping("/info")
    public CommonResult<UserVo> info() {
        String userId = StpUtil.getLoginIdAsString();
        UserVo userVo = userService.info(userId);
        return CommonResult.success(userVo);
    }

    @Operation(summary = "修改")
    @PostMapping("/update")
    public CommonResult<Object> update(@RequestBody UserVo userVo) {
        userVo.setId(StpUtil.getLoginIdAsString());
        userService.updateWithClear(userVo);
        return CommonResult.success();
    }

    @Operation(summary = "增加浏览次数")
    @GetMapping("/addViewNum")
    public CommonResult<Object> addViewNum() {
        userService.addViewNum();
        return CommonResult.success();
    }

    @Operation(summary = "获取浏览次数")
    @GetMapping("/getViewNum")
    public CommonResult<Integer> getViewNum() {
        Integer viewNum = userService.getViewNum();
        return CommonResult.success(viewNum);
    }

}
