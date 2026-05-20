package com.whtc.employee.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批历史记录VO
 */
@Data
public class ApprovalHistoryVO {

    /**
     * 历史记录ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批结果：1-通过 2-拒绝
     */
    private Integer approvalStatus;

    /**
     * 审批结果描述
     */
    private String approvalStatusName;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
