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
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
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
        Set<Long> visited = new HashSet<>();
        findChildDeptIds(allDepts, deptId, childIds, visited);

        return childIds;
    }

    /**
     * 递归查找子部门ID（带循环引用防护）
     * @param allDepts 所有部门列表
     * @param parentId 父部门ID
     * @param result 结果列表
     * @param visited 已访问部门ID集合（防止循环引用死循环）
     */
    private void findChildDeptIds(List<Department> allDepts, Long parentId, List<Long> result, Set<Long> visited) {
        for (Department dept : allDepts) {
            // 修复：处理parentId为null的情况，同时支持0和null作为根部门标识
            boolean isMatch = (parentId == null && dept.getParentId() == null)
                           || (parentId != null && parentId.equals(dept.getParentId()));
            if (isMatch) {
                // 防止循环引用：如果已经访问过则跳过
                if (visited.contains(dept.getId())) {
                    continue;
                }
                visited.add(dept.getId());
                result.add(dept.getId());
                // 递归查找该部门的子部门
                findChildDeptIds(allDepts, dept.getId(), result, visited);
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

        // 构建部门树 - 使用Map预处理优化性能，时间复杂度O(n)
        // 优先尝试parentId=0，如果没有则尝试parentId=null
        boolean hasRootWithZero = allDepts.stream()
                .anyMatch(d -> d.getParentId() != null && d.getParentId() == 0);
        Long rootParentId = hasRootWithZero ? 0L : null;
        List<DeptTreeVO> tree = buildDeptTreeOptimized(allDepts, deptEmployeeCount, rootParentId);

        return tree;
    }

    /**
     * 优化后的部门树构建（使用Map预处理，时间复杂度O(n)）
     * @param allDepts 所有部门列表
     * @param deptEmployeeCount 部门员工数量映射
     * @param rootParentId 根部门父ID
     * @return 部门树列表
     */
    private List<DeptTreeVO> buildDeptTreeOptimized(List<Department> allDepts, Map<Long, Long> deptEmployeeCount, Long rootParentId) {
        // 使用Map按parentId分组，避免每次递归都遍历全部部门
        Map<Long, List<Department>> deptMap = allDepts.stream()
                .collect(Collectors.groupingBy(
                        dept -> dept.getParentId() != null ? dept.getParentId() : -1L,
                        Collectors.toList()
                ));

        // 递归构建树
        return buildDeptTreeRecursive(deptMap, deptEmployeeCount,
                rootParentId != null ? rootParentId : -1L, new HashSet<>());
    }

    /**
     * 递归构建部门树（带循环引用防护）
     * @param deptMap 按parentId分组的部门Map
     * @param deptEmployeeCount 部门员工数量映射
     * @param parentId 父部门ID
     * @param visited 已访问部门ID集合（防止循环引用）
     * @return 部门树列表
     */
    private List<DeptTreeVO> buildDeptTreeRecursive(Map<Long, List<Department>> deptMap,
                                                      Map<Long, Long> deptEmployeeCount,
                                                      Long parentId, Set<Long> visited) {
        List<DeptTreeVO> result = new ArrayList<>();
        List<Department> children = deptMap.getOrDefault(parentId, Collections.emptyList());

        for (Department dept : children) {
            // 防止循环引用
            if (visited.contains(dept.getId())) {
                continue;
            }
            visited.add(dept.getId());

            DeptTreeVO vo = new DeptTreeVO();
            vo.setId(dept.getId());
            vo.setName(dept.getName());
            vo.setParentId(dept.getParentId());

            // 递归构建子部门树
            List<DeptTreeVO> childNodes = buildDeptTreeRecursive(deptMap, deptEmployeeCount, dept.getId(), visited);
            vo.setChildren(childNodes);

            // 计算员工总数（本部门 + 所有子部门）
            int count = calculateEmployeeCountOptimized(dept.getId(), deptEmployeeCount, deptMap, new HashSet<>());
            vo.setEmployeeCount(count);

            result.add(vo);
        }

        return result;
    }

    /**
     * 优化后的员工数量计算（使用Map预处理）
     * @param deptId 部门ID
     * @param deptEmployeeCount 部门员工数量映射
     * @param deptMap 按parentId分组的部门Map
     * @param visited 已访问部门ID集合（防止循环引用）
     * @return 员工总数
     */
    private int calculateEmployeeCountOptimized(Long deptId, Map<Long, Long> deptEmployeeCount,
                                                Map<Long, List<Department>> deptMap, Set<Long> visited) {
        // 防止循环引用
        if (visited.contains(deptId)) {
            return 0;
        }
        visited.add(deptId);

        // 本部门员工数
        long count = deptEmployeeCount.getOrDefault(deptId, 0L);

        // 加上所有子部门的员工数
        List<Department> children = deptMap.getOrDefault(deptId, Collections.emptyList());
        for (Department dept : children) {
            count += calculateEmployeeCountOptimized(dept.getId(), deptEmployeeCount, deptMap, visited);
        }

        return (int) count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "department", key = "'all'")
    public void deleteById(Long id) {
        // 1. 校验是否存在子部门
        LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(Department::getParentId, id);
        deptWrapper.eq(Department::getIsDeleted, 0);
        long childCount = this.count(deptWrapper);
        if (childCount > 0) {
            throw new RuntimeException("该部门存在子部门，无法删除");
        }

        // 2. 校验是否存在关联员工
        LambdaQueryWrapper<Employee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.eq(Employee::getDeptId, id);
        empWrapper.eq(Employee::getIsDeleted, 0);
        long empCount = employeeMapper.selectCount(empWrapper);
        if (empCount > 0) {
            throw new RuntimeException("该部门存在关联员工，无法删除");
        }

        // 3. 执行删除
        this.removeById(id);
    }

    @Override
    public int getDeptLevel(Long deptId) {
        if (deptId == null) {
            return 0;
        }

        int level = 1;
        Set<Long> visited = new HashSet<>();
        Long currentId = deptId;

        while (currentId != null) {
            // 防止循环引用
            if (visited.contains(currentId)) {
                break;
            }
            visited.add(currentId);

            Department dept = this.getById(currentId);
            if (dept == null || dept.getParentId() == null || dept.getParentId() == 0) {
                break;
            }
            currentId = dept.getParentId();
            level++;
        }

        return level;
    }
}
