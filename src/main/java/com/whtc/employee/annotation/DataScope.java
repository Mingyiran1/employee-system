package com.whtc.employee.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 * 作用于Service层方法，自动根据当前用户角色过滤数据
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门表的别名（用于SQL关联查询）
     * 例如：e.dept_id 中的 e
     */
    String deptAlias() default "";

    /**
     * 用户ID字段名（用于本人数据权限）
     * 例如：e.create_by 或 e.user_id
     */
    String userField() default "create_by";

    /**
     * 部门ID字段名
     * 例如：e.dept_id
     */
    String deptField() default "dept_id";
}
