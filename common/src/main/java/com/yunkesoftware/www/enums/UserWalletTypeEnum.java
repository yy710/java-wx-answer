package com.yunkesoftware.www.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum UserWalletTypeEnum {
    // 余额
    INTEGRAL(1, "积分")
    ;

    private final int key;
    private final String label;

    UserWalletTypeEnum(int key, String label) {
        this.key = key;
        this.label = label;
    }

    public static String keyToLabel(Integer key) {
        if (key != null) {
            for (UserWalletTypeEnum userWalletTypeEnum : UserWalletTypeEnum.values()) {
                if (Objects.equals(key, userWalletTypeEnum.key)) {
                    return userWalletTypeEnum.label;
                }
            }
        }
        return null;
    }
}
