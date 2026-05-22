package com.whtc.employee.constant;

/**
 * 正则表达式常量
 */
public class RegexConstant {

    // 私有构造函数防止实例化
    private RegexConstant() {
        throw new AssertionError("常量类不能被实例化");
    }

    /**
     * 手机号正则：1开头，第二位3-9，共11位
     */
    public static final String PHONE = "^1[3-9]\\d{9}$";
    public static final String PHONE_MESSAGE = "手机号格式不正确";
    public static final int PHONE_MAX_LENGTH = 11;

    /**
     * 邮箱正则 - 限制长度防止ReDoS攻击
     */
    public static final String EMAIL = "^[a-zA-Z0-9._%+-]{1,64}@[a-zA-Z0-9.-]{1,255}\\.[a-zA-Z]{2,10}$";
    public static final String EMAIL_MESSAGE = "邮箱格式不正确";
    public static final int EMAIL_MAX_LENGTH = 320;

    /**
     * 身份证号正则（宽松）：18位，前17位数字，最后一位数字或X/x
     */
    public static final String ID_CARD = "^\\d{17}[\\dXx]$";
    public static final String ID_CARD_MESSAGE = "身份证号格式不正确（18位，最后一位可为X）";
    public static final int ID_CARD_LENGTH = 18;

    /**
     * 固定电话/手机号通用正则 - 优化避免回溯
     */
    public static final String PHONE_OR_TEL = "^(?:1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$";
    public static final String PHONE_OR_TEL_MESSAGE = "电话格式不正确";
    public static final int PHONE_OR_TEL_MAX_LENGTH = 13;
}
