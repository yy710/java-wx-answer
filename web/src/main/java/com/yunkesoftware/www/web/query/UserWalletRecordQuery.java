package com.yunkesoftware.www.web.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.query.PageCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserWalletRecordQuery extends PageCurrency {
    @Schema(description = "钱包Id")
    private String walletId;

    @Schema(description = "1-收入 2-支出")
    private Integer InOut;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
