package com.yunkesoftware.www.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义异常
 *
 * @author fanyawei
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class YunKeException extends RuntimeException {
    /**
     * 错误代码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String message;

    public YunKeException(ExceptionEnum exceptionEnum) {
        super();
        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
    }


    public YunKeException(ExceptionEnum exceptionEnum, String message) {
        super();
        this.code = exceptionEnum.getCode();
        this.message = message;
    }


}

