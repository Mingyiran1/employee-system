package com.whtc.employee.aspect;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.whtc.employee.annotation.DataScope;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.context.DataScopeContext;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.mapper.EmployeeMapper;
import com.whtc.employee.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限过滤切面
 */
@Aspect
@Component
@Slf4j
public class DataScopeAspect {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 在执行方法前进行数据权限过滤
     * 将过滤条件存入ThreadLocal，供Service层使用
     */
    @Before("@annotation(dataScope)")
    public void before(DataScope dataScope) {
        // 获取当前登录用户
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            log.warn("【数据权限】当前用户未登录，跳过数据权限过滤");
            return;
        }

        log.info("【数据权限】当前用户：{}，角色：{}，角色ID：{}", currentUser.getUsername(), currentUser.getRoleCode(), currentUser.getRoleId());

        // 如果是超级管理员，不进行数据过滤
        if ("admin".equals(currentUser.getRoleCode()) || currentUser.getRoleId() != null && currentUser.getRoleId() == 1) {
            log.info("【数据权限】当前用户是超级管理员，查看全部数据");
            return;
        }

        // 创建QueryWrapper并添加数据权限条件
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        Integer dataScopeValue = getDataScopeByRole(currentUser);
        String deptField = dataScope.deptField();
        String userField = dataScope.userField();

        log.info("【数据权限】数据范围值：{}，部门字段：{}，用户字段：{}", dataScopeValue, deptField, userField);

        switch (dataScopeValue) {
            case 1: // 全部数据
                log.info("【数据权限】全部数据，不过滤");
                return; // 不需要设置wrapper
            case 2: // 本部门及以下
                Long userDeptId = getCurrentUserDeptId(currentUser);
                log.info("【数据权限】本部门及以下，用户所属部门ID：{}", userDeptId);
                if (userDeptId != null) {
                    List<Long> childDeptIds = departmentService.getChildDeptIds(userDeptId);
                    childDeptIds.add(userDeptId);
                    log.info("【数据权限】部门及子部门ID列表：{}", childDeptIds);
                    wrapper.in(deptField, childDeptIds);
                }
                break;
            case 3: // 本部门
                Long deptId = getCurrentUserDeptId(currentUser);
                log.info("【数据权限】本部门，部门ID：{}", deptId);
                if (deptId != null) {
                    wrapper.eq(deptField, deptId);
                }
                break;
            case 4: // 仅本人
                log.info("【数据权限】仅本人，用户ID：{}", currentUser.getId());
                wrapper.eq(userField, currentUser.getId());
                break;
            default:
                log.warn("【数据权限】未知的数据权限范围：{}", dataScopeValue);
                break;
        }

        // 将wrapper存入ThreadLocal
        DataScopeContext.setWrapper(wrapper);
        log.info("【数据权限】数据权限条件已设置到ThreadLocal");
    }

    /**
     * 方法执行后清理ThreadLocal
     */
    @After("@annotation(dataScope)")
    public void after(DataScope dataScope) {
        DataScopeContext.clear();
        log.debug("【数据权限】清理ThreadLocal");
    }

    /**
     * 根据角色获取数据权限范围
     */
    private Integer getDataScopeByRole(SysUser user) {
        // 默认仅本人
        if (user.getRoleId() == null) {
            return 4;
        }
        // 根据角色ID判断
        return switch (user.getRoleId().intValue()) {
            case 1 -> 1; // admin - 全部
            case 2 -> 2; // dept_CEO - 本部门及以下
            case 3 -> 3; // dept_manager - 本部门
            case 4 -> 4; // user - 仅本人
            default -> 4;
        };
    }

    /**
     * 获取当前用户所属部门ID
     * 通过 employee.user_id = sys_user.id 关联查询
     */
    private Long getCurrentUserDeptId(SysUser user) {
        if (user == null || user.getId() == null) {
            log.warn("【数据权限】获取用户部门ID失败：用户信息为空");
            return null;
        }

        // 通过 employee 表查询当前登录用户对应的部门ID
        // 逻辑：当前登录用户(sys_user.id) -> 员工表(employee.user_id) -> 部门ID(employee.dept_id)
        log.info("【数据权限】查询用户 {} 的部门ID", user.getId());
        Long deptId = employeeMapper.selectDeptIdByUserId(user.getId());

        if (deptId == null) {
            log.warn("【数据权限】用户 {} 未关联员工信息，无法获取部门ID", user.getId());
        } else {
            log.info("【数据权限】用户 {} 的部门ID：{}", user.getId(), deptId);
        }

        return deptId;
    }
}
