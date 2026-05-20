package com.whtc.employee.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批详情VO
 */
@Data
public class ApprovalDetailVO {

    /**
     * 审批记录ID
     */
    private Long id;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务类型描述
     */
    private String businessTypeName;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 当前审批状态：0-待审批 1-已通过 2-已拒绝
     */
    private Integer approvalStatus;

    /**
     * 审批状态描述
     */
    private String approvalStatusName;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 员工姓名（业务对象名称）
     */
    private String employeeName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 审批历史记录
     */
    private List<ApprovalHistoryVO> historyList;
}
