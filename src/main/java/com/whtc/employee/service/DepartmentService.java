package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.entity.Department;
import com.whtc.employee.vo.DeptTreeVO;

import java.util.List;

public interface DepartmentService extends IService<Department> {

    List<Department> getAllDepartments();

    /**
     * 获取指定部门的所有子部门ID列表
     * @param deptId 部门ID
     * @return 子部门ID列表（不包含传入的部门ID）
     */
    List<Long> getChildDeptIds(Long deptId);

    /**
     * 获取部门树形结构，包含员工数量统计
     * @return 部门树列表
     */
    List<DeptTreeVO> getDeptTree();
}
