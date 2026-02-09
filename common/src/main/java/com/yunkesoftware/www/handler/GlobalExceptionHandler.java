package com.yunkesoftware.www.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.result.CommonResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.UnknownHostException;


@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

    /**
     * 处理自定义的业务异常
     */
    @ExceptionHandler(YunKeException.class)
    @ResponseBody
    public CommonResult bizExceptionHandler(YunKeException e) {
        return new CommonResult(e.getCode(), e.getMessage());
    }


    /**
     * 处理其他异常
     */

    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    public CommonResult handlerNotLoginException(NotLoginException nle) {

//        // 打印堆栈，以供调试
//        nle.printStackTrace();
//        System.out.println("");
        // 判断场景值，定制化异常信息
        String message = "";
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未提供token";
        } else if (nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "token无效";
        } else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "token已过期";
        } else if (nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "token已被顶下线";
        } else if (nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "token已被踢下线";
        } else {
            message = "当前会话未登录";
        }
        return CommonResult.unauthorized(message);
    }

    /**
     * 快递一百异常拦截
     * ConnectTimeoutException
     */

    @ExceptionHandler(UnknownHostException.class)
    @ResponseBody
    public CommonResult handlerConnectTimeoutException(UnknownHostException nle) {

        return CommonResult.failed("第三方服务网络异常");
    }


    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult exceptionHandler(MethodArgumentNotValidException e) {
        return CommonResult.validateFailed(e.getBindingResult().getFieldError().getDefaultMessage());
    }

}
