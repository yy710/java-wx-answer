package com.yunkesoftware.www.adm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DataAnalysisVo implements Serializable {
    @Schema(description = "今日新增用户数")
    private Long todayUserNum;
    @Schema(description = "总用户数")
    private Long totalUserNum;

    @Schema(description = "今日投票数")
    private Long todayTicketNum;
    @Schema(description = "总投票数")
    private Long totalTicketNum;

    @Schema(description = "今日获得积分用户数")
    private Long todayIntegralUserNum;
    @Schema(description = "今日获得积分总数")
    private BigDecimal todayIntegralAmount;

    @Schema(description = "总获得积分用户数")
    private Long totalIntegralUserNum;
    @Schema(description = "总获得积分总数")
    private BigDecimal totalIntegralAmount;
}
