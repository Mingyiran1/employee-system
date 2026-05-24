package com.whtc.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whtc.employee.entity.InsuranceEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InsuranceEmployeeMapper extends BaseMapper<InsuranceEmployee> {

    @Select("SELECT e.*, ic.name as company_name, s.name as supplier_name " +
            "FROM insurance_employee e " +
            "LEFT JOIN insured_company ic ON e.company_id = ic.id " +
            "LEFT JOIN supplier s ON e.supplier_id = s.id " +
            "WHERE e.is_deleted = 0 AND e.id = #{id}")
    InsuranceEmployee selectEmployeeWithCompany(Long id);

    @Select("SELECT e.*, ic.name as company_name, s.name as supplier_name " +
            "FROM insurance_employee e " +
            "LEFT JOIN insured_company ic ON e.company_id = ic.id " +
            "LEFT JOIN supplier s ON e.supplier_id = s.id " +
            "WHERE e.is_deleted = 0 ORDER BY e.create_time DESC")
    List<InsuranceEmployee> selectEmployeeListWithCompany();

    /**
     * 根据创建人ID查询员工列表（用于数据权限）
     */
    @Select("SELECT * FROM insurance_employee WHERE create_by = #{userId} AND is_deleted = 0")
    List<InsuranceEmployee> selectByCreateBy(Long userId);
}
