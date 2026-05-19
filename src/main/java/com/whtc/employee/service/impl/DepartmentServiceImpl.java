package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.entity.Department;
import com.whtc.employee.mapper.DepartmentMapper;
import com.whtc.employee.service.DepartmentService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

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
            if (parentId.equals(dept.getParentId())) {
                result.add(dept.getId());
                // 递归查找该部门的子部门
                findChildDeptIds(allDepts, dept.getId(), result);
            }
        }
    }
}
