package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.dto.EmployeeDTO;
import com.whtc.employee.dto.EmployeePageQueryDTO;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.vo.EmployeeVO;

import java.util.List;

public interface EmployeeService extends IService<Employee> {

    /**
     * 员工分页查询（自动应用数据权限）
     * @param employeePageQueryDTO 查询条件
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 新增员工
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 更新员工
     */
    void update(EmployeeDTO employeeDTO);

    /**
     * 根据ID查询员工
     */
    EmployeeVO getById(Long id);

    /**
     * 根据ID删除员工
     */
    void deleteById(Long id);

    /**
     * 批量删除员工
     */
    void deleteByIds(List<Long> ids);
}
