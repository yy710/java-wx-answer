package com.alibaba.excel.annotation;

import com.alibaba.excel.converters.AutoConverter;
import com.alibaba.excel.converters.Converter;

import java.lang.annotation.*;

/**
 * @author guozhijie
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelProperty {


    String[] value() default {""};

    int index() default -1;

    int order() default Integer.MAX_VALUE;

    Class<? extends Converter> converter() default AutoConverter.class;

    /** @deprecated */
    @Deprecated
    String format() default "";
    String dictType() default "";
}
