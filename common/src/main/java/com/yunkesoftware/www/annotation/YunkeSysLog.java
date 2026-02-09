package com.yunkesoftware.www.annotation;


import com.yunkesoftware.www.constant.SysLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AOP
 *
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface YunkeSysLog {

    String value() default "";

    String type() default SysLog.adm_SYS_LOG;
}
