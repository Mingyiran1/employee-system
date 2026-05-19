package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.entity.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {

    List<Department> getAllDepartments();

    /**
     * 获取指定部门的所有子部门ID列表
     * @param deptId 部门ID
     * @return 子部门ID列表（不包含传入的部门ID）
     */
    List<Long> getChildDeptIds(Long deptId);
}
