package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.vo.UserMultiVo;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "用户访问信息")
@RestController
@RequestMapping("/sys/userMulti")
public class UserMultiController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @PostMapping("/page")
    public CommonResult<Page<UserMultiVo>> page(@RequestBody PageCurrency pageQuery) {
        Page<UserMultiVo> pageParam = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        List<UserMultiVo> resultList = new ArrayList<>();
//        String userMulti = stringRedisTemplate.opsForValue().get(RedisKey.USER_MULTI);
//        if (userMulti != null) {
//            UserMultiVo userMultiVo = new UserMultiVo();
//            userMultiVo.setUserMultiple(Integer.parseInt(userMulti));
//            resultList.add(userMultiVo);
//        }
        pageParam.setRecords(resultList);
        return CommonResult.success(pageParam);
    }

    @PostMapping("addOrModify")
    public CommonResult<Object> addOrModify(@RequestBody UserMultiVo userMultiVo) {
        stringRedisTemplate.opsForValue().set(RedisKey.USER_MULTI, String.valueOf(userMultiVo.getUserMultiple()));
        return CommonResult.success();
    }

}
