package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer gender;

    private String phone;

    private String email;

    private String idCard;

    private Long deptId;

    private String position;

    private LocalDate entryDate;

    private Integer status;

    private String address;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建人ID（关联sys_user表，用于数据权限）
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 关联的系统用户ID
     */
    @TableField("user_id")
    private Long userId;

    // 非数据库字段 - 部门名称
    @TableField(exist = false)
    private String deptName;
}
