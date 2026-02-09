package com.yunkesoftware.www.exception;

import lombok.Getter;

@Getter
public enum ExceptionEnum {

    FAIL(500, "失败"), //
    ILLEGAL_ARGUMENT(99998, "非法参数"), //
    ;
    private Integer code;
    private String message;

    ExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
