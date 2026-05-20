package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_node")
public class ApprovalNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long processId;

    private String nodeName;

    private Long roleId;

    private String roleCode;

    /**
     * 所属部门ID（null表示通用节点）
     */
    private Long deptId;

    private Integer nodeOrder;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
