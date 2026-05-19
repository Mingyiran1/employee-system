package com.whtc.employee.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户数据权限上下文
 * 用于在Service层传递当前用户的数据权限信息
 */
@Data
public class DataScopeContext {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 数据权限范围：1全部 2本部门及以下 3本部门 4仅本人
     */
    private Integer dataScope;

    /**
     * 部门ID（当前用户所属部门）
     */
    private Long deptId;

    /**
     * 可查看的部门ID列表（用于本部门及以下权限）
     */
    private List<Long> deptIds;
}
