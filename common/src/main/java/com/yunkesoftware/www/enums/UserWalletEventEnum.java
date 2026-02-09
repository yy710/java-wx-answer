package com.yunkesoftware.www.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum UserWalletEventEnum {
    RISK_READ(1, "风险提示阅读"),
    TOPIC_REWARD(2, "答题奖励"),
    SCAN_PAY(3, "扫码支付"),
    DAILY_SIGN(4, "日常签到"),
    INVITE_REWARD(5, "邀请新人"),
    VIDEO(6, "视频奖励")
    ;

    private final int key;
    private final String label;

    UserWalletEventEnum(int key, String label) {
        this.key = key;
        this.label = label;
    }

    public static String keyToLabel(Integer key) {
        if (key != null) {
            for (UserWalletEventEnum userWalletEventEnum : UserWalletEventEnum.values()) {
                if (Objects.equals(key, userWalletEventEnum.key)) {
                    return userWalletEventEnum.label;
                }
            }
        }
        return null;
    }
}
