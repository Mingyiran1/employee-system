package com.whtc.employee.dto;

import lombok.Data;

/**
 * 审批流程DTO
 */
@Data
public class ApprovalProcessDTO {

    /**
     * 业务类型（EMPLOYEE_ENTRY/EMPLOYEE_LEAVE）
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 审批结果：1-通过 2-拒绝
     */
    private Integer approvalStatus;

    /**
     * 审批意见
     */
    private String comment;
}
