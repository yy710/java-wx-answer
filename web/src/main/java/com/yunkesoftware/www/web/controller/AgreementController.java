package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.web.entity.Agreement;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.AgreementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "协议")
@RestController
@RequestMapping("/wx/agreement")
public class AgreementController {
    @Resource
    private AgreementService agreementService;

    @GetMapping("/getOneByType")
    public CommonResult<Agreement> getOneByType(Integer type) {
        Agreement agreement = agreementService.getOne(new LambdaQueryWrapper<Agreement>()
                .eq(Agreement::getType, type)
                .last("LIMIT 1"));
        return CommonResult.success(agreement);
    }
}
