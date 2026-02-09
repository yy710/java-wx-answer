package com.yunkesoftware.www.web.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScanPayQuery {
    @Schema(description = "支付金额")
    @NotNull(message = "支付金额不能为空")
    @Min(value = 0, message = "支付金额不能小于0")
    private BigDecimal amount;

    @Schema(description = "收款方id")
    @NotBlank(message = "收款方id不能为空")
    private String paymentId;
}
