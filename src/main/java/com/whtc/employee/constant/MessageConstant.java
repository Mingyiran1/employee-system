package com.whtc.employee.constant;

/**
 * 消息常量
 */
public class MessageConstant {

    // 私有构造函数防止实例化
    private MessageConstant() {
        throw new AssertionError("常量类不能被实例化");
    }

    public static final String LOGIN_FAILED = "登录失败，用户名或密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_EXISTS = "账号已存在";
    public static final String OPERATION_SUCCESS = "操作成功";
    public static final String OPERATION_FAILED = "操作失败";
    public static final String SYSTEM_ERROR = "系统繁忙，请稍后重试";
    public static final String PARAM_ERROR = "参数错误";
    public static final String DELETE_SUCCESS = "删除成功";
    public static final String DELETE_FAILED = "删除失败，该数据已被使用";
}
