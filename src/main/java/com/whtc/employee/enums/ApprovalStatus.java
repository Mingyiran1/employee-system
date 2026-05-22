package com.whtc.employee.enums;

/**
 * 审批状态枚举
 */
public enum ApprovalStatus {

    /**
     * 待审批
     */
    PENDING(0, "待审批"),

    /**
     * 已通过
     */
    APPROVED(1, "已通过"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 已撤销
     */
    CANCELLED(3, "已撤销");

    private final Integer code;
    private final String name;

    ApprovalStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取枚举
     */
    public static ApprovalStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据code获取名称
     */
    public static String getNameByCode(Integer code) {
        ApprovalStatus status = fromCode(code);
        return status != null ? status.getName() : "未知";
    }

    /**
     * 检查状态是否为终态（不可再变更）
     */
    public static boolean isFinalStatus(Integer code) {
        return APPROVED.getCode().equals(code)
                || REJECTED.getCode().equals(code)
                || CANCELLED.getCode().equals(code);
    }

    /**
     * 检查状态是否允许审批操作
     */
    public static boolean isPending(Integer code) {
        return PENDING.getCode().equals(code);
    }
}
