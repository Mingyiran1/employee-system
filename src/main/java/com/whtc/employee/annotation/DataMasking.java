package com.whtc.employee.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.whtc.employee.enums.MaskingType;
import com.whtc.employee.jackson.DataMaskingSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解
 * 用于标记需要脱敏的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = DataMaskingSerializer.class)
public @interface DataMasking {

    /**
     * 脱敏类型
     */
    MaskingType value();

    /**
     * 是否对管理员显示明文（默认否）
     */
    boolean maskForAdmin() default false;
}
