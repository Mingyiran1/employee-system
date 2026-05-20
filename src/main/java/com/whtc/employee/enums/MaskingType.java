package com.whtc.employee.enums;

/**
 * 数据脱敏类型枚举
 */
public enum MaskingType {

    /**
     * 手机号：13800138000 → 138****8000
     */
    PHONE,

    /**
     * 邮箱：zhangsan@qq.com → zhan***@qq.com
     */
    EMAIL,

    /**
     * 身份证号：110101199001011234 → 110101********1234
     */
    ID_CARD,

    /**
     * 姓名：张三 → 张*
     */
    NAME,

    /**
     * 地址：北京市朝阳区xxx街道 → 北京市朝阳区***
     */
    ADDRESS,

    /**
     * 银行卡：6222021234567890123 → 622202*********0123
     */
    BANK_CARD,

    /**
     * 通用：显示前3后4，中间用****代替
     */
    DEFAULT
}
