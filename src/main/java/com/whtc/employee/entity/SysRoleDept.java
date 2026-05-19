package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色部门关联实体类（用于自定义数据权限）
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
