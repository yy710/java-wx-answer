package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.PaymentService;
import com.yunkesoftware.www.adm.entity.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-16
 */
@Tag(name = "活动支付收款信息")
@RestController
@RequestMapping("/sys/payment")
public class PaymentController {
    @Resource
    private PaymentService paymentService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<Payment>> page(@RequestBody Payment payment) {
        Page<Payment> pageResult = paymentService.pageByQuery(payment);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Payment> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(paymentService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody Payment payment) {
        return CommonResult.status(paymentService.saveOrUpdate(payment));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(paymentService.removeByIds(ids));
    }
}
