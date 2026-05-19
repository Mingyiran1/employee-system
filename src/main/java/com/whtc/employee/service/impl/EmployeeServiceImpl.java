package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.context.DataScopeContext;
import com.whtc.employee.dto.EmployeeDTO;
import com.whtc.employee.dto.EmployeePageQueryDTO;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.mapper.EmployeeMapper;
import com.whtc.employee.service.EmployeeService;
import com.whtc.employee.vo.EmployeeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        Page<Employee> pageInfo = new Page<>(employeePageQueryDTO.getPage(), employeePageQueryDTO.getSize());

        // 从ThreadLocal获取数据权限条件（如果存在）
        QueryWrapper<Employee> wrapper = DataScopeContext.getWrapper();
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        // 模糊查询姓名
        if (StringUtils.hasText(employeePageQueryDTO.getName())) {
            wrapper.like("name", employeePageQueryDTO.getName());
        }
        // 按部门筛选
        if (employeePageQueryDTO.getDeptId() != null) {
            wrapper.eq("dept_id", employeePageQueryDTO.getDeptId());
        }
        // 按状态筛选
        if (employeePageQueryDTO.getStatus() != null) {
            wrapper.eq("status", employeePageQueryDTO.getStatus());
        }

        wrapper.orderByDesc("create_time");
        Page<Employee> pageData = this.page(pageInfo, wrapper);

        // 转换为VO
        List<EmployeeVO> records = pageData.getRecords().stream().map(emp -> {
            EmployeeVO vo = new EmployeeVO();
            BeanUtils.copyProperties(emp, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult(pageData.getTotal(), records);
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 默认在职
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        // 设置创建人（当前登录用户）
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser != null) {
            employee.setCreateBy(currentUser.getId());
        }
        this.save(employee);
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        this.updateById(employee);
    }

    @Override
    public EmployeeVO getById(Long id) {
        Employee employee = employeeMapper.selectEmployeeWithDept(id);
        if (employee == null) {
            return null;
        }
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(employee, vo);
        vo.setDeptName(employee.getDeptName());
        return vo;
    }

    @Override
    public void deleteById(Long id) {
        this.removeById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        this.removeByIds(ids);
    }
}
