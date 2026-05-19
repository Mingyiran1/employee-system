package com.whtc.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whtc.employee.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Select("SELECT e.*, d.name as dept_name FROM employee e " +
            "LEFT JOIN department d ON e.dept_id = d.id " +
            "WHERE e.is_deleted = 0 AND e.id = #{id}")
    Employee selectEmployeeWithDept(Long id);

    @Select("SELECT e.*, d.name as dept_name FROM employee e " +
            "LEFT JOIN department d ON e.dept_id = d.id " +
            "WHERE e.is_deleted = 0 ORDER BY e.create_time DESC")
    List<Employee> selectEmployeeListWithDept();

    /**
     * 根据系统用户ID查询员工所属部门ID
     * 用于数据权限：获取当前登录用户对应的部门
     */
    @Select("SELECT dept_id FROM employee WHERE user_id = #{userId} AND is_deleted = 0 LIMIT 1")
    Long selectDeptIdByUserId(Long userId);

    /**
     * 根据员工ID查询系统用户ID
     */
    @Select("SELECT user_id FROM employee WHERE id = #{employeeId} AND is_deleted = 0 LIMIT 1")
    Long selectUserIdByEmployeeId(Long employeeId);
}
