package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_history")
public class ApprovalHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private Long nodeId;

    private Long approverId;

    private Integer approvalStatus;

    private String approvalComment;

    private LocalDateTime approvalTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
