package com.yunkesoftware.www.web.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/** 统一奖励接口返回的可审计结果；积分使用两位小数字符串，避免前端浮点误差。 */
@Getter
@Setter
@Accessors(chain = true)
public class RewardPositiveResult {
    private boolean completed;
    private String requestedPoints;
    private String awardedPoints;
    private String awardReason;
    private String dailyEarnedPoints;
    private String dailyRemainingPoints;
    private String walletPoints;
    private String walletRecordId;
    private boolean duplicate;
}
