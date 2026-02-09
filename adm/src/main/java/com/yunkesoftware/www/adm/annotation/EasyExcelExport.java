package com.yunkesoftware.www.adm.annotation;

import java.lang.annotation.*;

/**
 * <p>动态参数导出excel</p>
 * IS_EXPORT(是否导出): true 导出 必传
 * FIELD_NAME(自定义字段值): 可不传，扩展字段，特殊场景使用
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EasyExcelExport {

    /**
     * 文件名称
     */
    String fileName() default "";


}

