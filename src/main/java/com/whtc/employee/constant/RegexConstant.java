package com.whtc.employee.constant;

/**
 * 正则表达式常量
 */
public class RegexConstant {

    /**
     * 手机号正则：1开头，第二位3-9，共11位
     */
    public static final String PHONE = "^1[3-9]\\d{9}$";
    public static final String PHONE_MESSAGE = "手机号格式不正确";

    /**
     * 邮箱正则
     */
    public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String EMAIL_MESSAGE = "邮箱格式不正确";

    /**
     * 身份证号正则（宽松）：18位，前17位数字，最后一位数字或X/x
     */
    public static final String ID_CARD = "^\\d{17}[\\dXx]$";
    public static final String ID_CARD_MESSAGE = "身份证号格式不正确（18位，最后一位可为X）";

    /**
     * 固定电话/手机号通用正则
     */
    public static final String PHONE_OR_TEL = "^(1[3-9]\\d{9})|(0\\d{2,3}-?\\d{7,8})$";
    public static final String PHONE_OR_TEL_MESSAGE = "电话格式不正确";
}
