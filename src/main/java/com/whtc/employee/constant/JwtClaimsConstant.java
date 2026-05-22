package com.whtc.employee.constant;

/**
 * JWT Claims 常量
 */
public class JwtClaimsConstant {

    // 私有构造函数防止实例化
    private JwtClaimsConstant() {
        throw new AssertionError("常量类不能被实例化");
    }

    public static final String EMP_ID = "empId";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
}