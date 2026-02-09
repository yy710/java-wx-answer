package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.adm.entity.User;
import com.yunkesoftware.www.adm.service.UserService;

import com.yunkesoftware.www.annotation.YunkeSysLog;

import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author cuiyq
 * @since 2025-02-13
 */
@Tag(name = "用户表")
@RestController
@RequestMapping("/sys/user")
public class UserController {
    @Resource
    private UserService userService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<User>> page(@RequestBody User user) {
        Page<User> pageResult = userService.pageByQuery(user);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<User> getOneById(@RequestParam("id") String id) {
        User user = userService.getOneById(id);
        return CommonResult.success(user);
    }

    //    @Operation(summary = "通过Id批量删除")
//    @DeleteMapping("/delete")
//    @YunkeSysLog("用户表-删除")
//    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
//        userService.delete(ids);
//        return CommonResult.status(true);
//    }
    @Operation(summary = "修改")
    @PostMapping("/modify")
    @YunkeSysLog("用户表-修改")
    public CommonResult<Object> modify(@RequestBody User user) {
        userService.modify(user);
        return CommonResult.success();
    }

}
