package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.web.entity.User;
import com.yunkesoftware.www.web.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private UserService userService;


    @GetMapping("/fix01")
    public void fix01() {
        List<User> userList = userService.list();

    }
}
