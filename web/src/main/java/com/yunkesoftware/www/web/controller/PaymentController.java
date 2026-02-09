package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.PaymentService;
import com.yunkesoftware.www.web.entity.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-19
 */
@Tag(name = "活动支付信息")
@RestController
@RequestMapping("/wx/payment")
public class PaymentController {
    @Resource
    private PaymentService paymentService;

    @Operation(summary ="获取全部列表")
    @PostMapping("/list")
    public CommonResult<List<Payment>> list(@RequestBody Payment payment) {
        return CommonResult.success(paymentService.list(new LambdaQueryWrapper<>(payment)));
    }
}
