package com.whtc.employee.vo;

import lombok.Data;

import java.util.List;

@Data
public class DeptTreeVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer employeeCount;  // 该部门及子部门的员工总数
    private List<DeptTreeVO> children;  // 子部门
}
