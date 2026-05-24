package com.whtc.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批流程DTO
 */
@Data
public class ApprovalProcessDTO {

    /**
     * 业务类型（EMPLOYEE_ENTRY/EMPLOYEE_LEAVE）
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /**
     * 业务ID
     */
    @NotNull(message = "业务ID不能为空")
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
