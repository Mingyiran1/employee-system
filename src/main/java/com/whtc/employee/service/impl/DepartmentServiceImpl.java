package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.entity.Department;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.mapper.DepartmentMapper;
import com.whtc.employee.mapper.EmployeeMapper;
import com.whtc.employee.service.DepartmentService;
import com.whtc.employee.vo.DeptTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    @Cacheable(value = "department", key = "'all'")
    public List<Department> getAllDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Department::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    @CacheEvict(value = "department", key = "'all'")
    public boolean save(Department department) {
        return super.save(department);
    }

    @Override
    @CacheEvict(value = "department", key = "'all'")
    public boolean updateById(Department department) {
        return super.updateById(department);
    }

    @Override
    @CacheEvict(value = "department", key = "'all'")
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    public List<Long> getChildDeptIds(Long deptId) {
        if (deptId == null) {
            return new ArrayList<>();
        }

        // 获取所有部门
        List<Department> allDepts = this.list();

        // 递归获取所有子部门ID
        List<Long> childIds = new ArrayList<>();
        findChildDeptIds(allDepts, deptId, childIds);

        return childIds;
    }

    /**
     * 递归查找子部门ID
     */
    private void findChildDeptIds(List<Department> allDepts, Long parentId, List<Long> result) {
        for (Department dept : allDepts) {
            // 修复：处理parentId为null的情况，同时支持0和null作为根部门标识
            boolean isMatch = (parentId == null && dept.getParentId() == null)
                           || (parentId != null && parentId.equals(dept.getParentId()));
            if (isMatch) {
                result.add(dept.getId());
                // 递归查找该部门的子部门
                findChildDeptIds(allDepts, dept.getId(), result);
            }
        }
    }

    @Override
    public List<DeptTreeVO> getDeptTree() {
        // 查询所有部门
        List<Department> allDepts = this.list();

        // 查询所有员工，按部门ID分组统计
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getIsDeleted, 0);
        List<Employee> allEmployees = employeeMapper.selectList(wrapper);
        Map<Long, Long> deptEmployeeCount = allEmployees.stream()
                .collect(Collectors.groupingBy(Employee::getDeptId, Collectors.counting()));

        // 构建部门树 - 从根部门开始（parentId为0或null的部门）
        // 优先尝试parentId=0，如果没有则尝试parentId=null
        boolean hasRootWithZero = allDepts.stream()
                .anyMatch(d -> d.getParentId() != null && d.getParentId() == 0);
        Long rootParentId = hasRootWithZero ? 0L : null;
        List<DeptTreeVO> tree = buildDeptTree(allDepts, deptEmployeeCount, rootParentId);

        return tree;
    }

    /**
     * 递归构建部门树
     * @param allDepts 所有部门列表
     * @param deptEmployeeCount 部门员工数量映射
     * @param parentId 父部门ID
     * @return 部门树列表
     */
    private List<DeptTreeVO> buildDeptTree(List<Department> allDepts, Map<Long, Long> deptEmployeeCount, Long parentId) {
        List<DeptTreeVO> result = new ArrayList<>();

        for (Department dept : allDepts) {
            // 修复：处理parentId为null的情况，同时支持0和null作为根部门标识
            boolean isMatch = (parentId == null && dept.getParentId() == null)
                           || (parentId != null && parentId.equals(dept.getParentId()));
            if (isMatch) {
                DeptTreeVO vo = new DeptTreeVO();
                vo.setId(dept.getId());
                vo.setName(dept.getName());
                vo.setParentId(dept.getParentId());

                // 递归构建子部门树
                List<DeptTreeVO> children = buildDeptTree(allDepts, deptEmployeeCount, dept.getId());
                vo.setChildren(children);

                // 计算员工总数（本部门 + 所有子部门）
                int count = calculateEmployeeCount(dept.getId(), deptEmployeeCount, allDepts);
                vo.setEmployeeCount(count);

                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 递归计算部门及子部门的员工总数
     * @param deptId 部门ID
     * @param deptEmployeeCount 部门员工数量映射
     * @param allDepts 所有部门列表
     * @return 员工总数
     */
    private int calculateEmployeeCount(Long deptId, Map<Long, Long> deptEmployeeCount, List<Department> allDepts) {
        // 本部门员工数
        long count = deptEmployeeCount.getOrDefault(deptId, 0L);

        // 加上所有子部门的员工数
        for (Department dept : allDepts) {
            if (deptId.equals(dept.getParentId())) {
                count += calculateEmployeeCount(dept.getId(), deptEmployeeCount, allDepts);
            }
        }

        return (int) count;
    }
}
