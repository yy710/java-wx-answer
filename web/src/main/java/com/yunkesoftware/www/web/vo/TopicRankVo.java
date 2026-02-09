package com.yunkesoftware.www.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopicRankVo {
    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "积分总数")
    private BigDecimal totalRewardAmount;
}
