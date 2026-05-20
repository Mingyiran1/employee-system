package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.context.DataScopeContext;
import com.whtc.employee.dto.EmployeeDTO;
import com.whtc.employee.dto.EmployeePageQueryDTO;
import com.whtc.employee.entity.Department;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.mapper.DepartmentMapper;
import com.whtc.employee.mapper.EmployeeMapper;
import com.whtc.employee.service.EmployeeService;
import com.whtc.employee.vo.EmployeeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

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
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
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
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
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
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
    public void deleteById(Long id) {
        this.removeById(id);
    }

    @Override
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
    public void deleteByIds(List<Long> ids) {
        this.removeByIds(ids);
    }

    @Override
    public List<EmployeeVO> listAllForApproval() {
        // 获取当前用户
        SysUser currentUser = BaseContext.getCurrentUser();

        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0)
               .eq("status", 1);  // 在职员工

        // 根据角色进行权限过滤
        if (currentUser != null && currentUser.getRoleId() != null) {
            Long roleId = currentUser.getRoleId();

            // 普通员工(roleId=4)：只能看到自己
            if (roleId == 4) {
                wrapper.eq("user_id", currentUser.getId());
            }
            // 部门经理(roleId=2)：只能看到自己管辖部门及子部门的员工
            else if (roleId == 2 && currentUser.getManagedDeptId() != null) {
                // 获取该部门及所有子部门的员工
                List<Long> deptIds = getDeptAndChildrenIds(currentUser.getManagedDeptId());
                wrapper.in("dept_id", deptIds);
            }
            // 管理员(roleId=1)和HR(roleId=3)：可以看到所有员工，不需要额外过滤
        }

        wrapper.orderByDesc("create_time");
        List<Employee> list = this.list(wrapper);

        // 转换为VO
        return list.stream().map(emp -> {
            EmployeeVO vo = new EmployeeVO();
            BeanUtils.copyProperties(emp, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取部门及其所有子部门的ID列表
     */
    private List<Long> getDeptAndChildrenIds(Long deptId) {
        List<Long> result = new ArrayList<>();
        result.add(deptId);

        // 递归获取子部门
        List<Long> childrenIds = getChildrenDeptIds(deptId);
        result.addAll(childrenIds);

        return result;
    }

    /**
     * 递归获取子部门ID
     */
    private List<Long> getChildrenDeptIds(Long parentId) {
        List<Long> result = new ArrayList<>();

        // 查询直接子部门
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
               .eq("is_deleted", 0);
        List<Department> children = departmentMapper.selectList(wrapper);

        for (Department child : children) {
            result.add(child.getId());
            // 递归获取孙部门
            result.addAll(getChildrenDeptIds(child.getId()));
        }

        return result;
    }

    @Override
    public EmployeeVO getByUserId(Long userId) {
        Employee employee = employeeMapper.selectByUserId(userId);
        if (employee == null) {
            return null;
        }
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(employee, vo);
        vo.setDeptName(employee.getDeptName());
        return vo;
    }
}
