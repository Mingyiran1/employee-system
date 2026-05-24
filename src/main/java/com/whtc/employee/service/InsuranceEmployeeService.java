package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.dto.InsuranceEmployeeDTO;
import com.whtc.employee.dto.InsuranceEmployeePageQueryDTO;
import com.whtc.employee.entity.InsuranceEmployee;
import com.whtc.employee.vo.InsuranceEmployeeVO;

import java.util.List;
import java.util.Map;

public interface InsuranceEmployeeService extends IService<InsuranceEmployee> {

    /**
     * 保险员工分页查询（自动应用数据权限）
     * @param queryDTO 查询条件
     */
    PageResult pageQuery(InsuranceEmployeePageQueryDTO queryDTO);

    /**
     * 新增保险员工
     */
    void saveEmployee(InsuranceEmployeeDTO employeeDTO);

    /**
     * 更新保险员工
     */
    void updateEmployee(InsuranceEmployeeDTO employeeDTO);

    /**
     * 根据ID查询保险员工
     */
    InsuranceEmployeeVO getEmployeeById(Long id);

    /**
     * 根据ID删除保险员工
     */
    void deleteById(Long id);

    /**
     * 批量删除保险员工
     */
    void deleteByIds(List<Long> ids);

    /**
     * 获取所有符合条件的保险员工列表
     */
    List<InsuranceEmployeeVO> listAll(InsuranceEmployeePageQueryDTO queryDTO);

    /**
     * 批量导入保险员工（带事务控制）
     * @param employees 员工列表
     * @return 导入结果 Map: successCount, errorCount, errorMessages
     */
    Map<String, Object> importBatch(List<InsuranceEmployee> employees);
}
