package com.whtc.employee.aspect;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.whtc.employee.annotation.DataScope;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.context.DataScopeContext;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.entity.SysRole;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.mapper.EmployeeMapper;
import com.whtc.employee.service.DepartmentService;
import com.whtc.employee.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

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

    @Autowired
    private SysRoleService sysRoleService;

    // SQL字段名校验正则：只允许字母、数字、下划线
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /**
     * 在执行方法前进行数据权限过滤
     * 将过滤条件存入ThreadLocal，供Service层使用
     */
    @Before("@annotation(dataScope)")
    public void before(DataScope dataScope) {
        try {
            // 获取当前登录用户
            SysUser currentUser = BaseContext.getCurrentUser();
            if (currentUser == null) {
                log.warn("【数据权限】当前用户未登录，跳过数据权限过滤");
                return;
            }

            log.debug("【数据权限】当前用户：{}，角色：{}，角色ID：{}", currentUser.getUsername(), currentUser.getRoleCode(), currentUser.getRoleId());

            // 如果是超级管理员，不进行数据过滤
            if ("admin".equals(currentUser.getRoleCode()) || currentUser.getRoleId() != null && currentUser.getRoleId() == 1) {
                log.debug("【数据权限】当前用户是超级管理员，查看全部数据");
                return;
            }

            // 校验字段名安全性（防止SQL注入）
            String deptField = dataScope.deptField();
            String userField = dataScope.userField();
            String deptAlias = dataScope.deptAlias();

            if (!isValidFieldName(deptField)) {
                log.error("【数据权限】部门字段名不合法：{}，跳过数据权限过滤", deptField);
                return;
            }
            if (!isValidFieldName(userField)) {
                log.error("【数据权限】用户字段名不合法：{}，跳过数据权限过滤", userField);
                return;
            }
            if (!deptAlias.isEmpty() && !isValidFieldName(deptAlias)) {
                log.error("【数据权限】表别名不合法：{}，跳过数据权限过滤", deptAlias);
                return;
            }

            // 构建带别名的字段名
            String fullDeptField = buildFieldWithAlias(deptAlias, deptField);
            String fullUserField = buildFieldWithAlias(deptAlias, userField);

            // 创建QueryWrapper并添加数据权限条件
            QueryWrapper<Employee> wrapper = new QueryWrapper<>();
            Integer dataScopeValue = getDataScopeByRole(currentUser);

            log.debug("【数据权限】数据范围值：{}，部门字段：{}，用户字段：{}", dataScopeValue, fullDeptField, fullUserField);

            switch (dataScopeValue) {
                case 1: // 全部数据
                    log.debug("【数据权限】全部数据，不过滤");
                    return; // 不需要设置wrapper
                case 2: // 本部门及以下
                    Long userDeptId = getCurrentUserDeptId(currentUser);
                    log.debug("【数据权限】本部门及以下，用户所属部门ID：{}", userDeptId);
                    if (userDeptId != null) {
                        List<Long> childDeptIds = departmentService.getChildDeptIds(userDeptId);
                        childDeptIds.add(userDeptId);
                        log.debug("【数据权限】部门及子部门ID列表：{}", childDeptIds);
                        wrapper.in(fullDeptField, childDeptIds);
                    }
                    break;
                case 3: // 本部门
                    Long deptId = getCurrentUserDeptId(currentUser);
                    log.debug("【数据权限】本部门，部门ID：{}", deptId);
                    if (deptId != null) {
                        wrapper.eq(fullDeptField, deptId);
                    }
                    break;
                case 4: // 仅本人
                    log.debug("【数据权限】仅本人，用户ID：{}", currentUser.getId());
                    wrapper.eq(fullUserField, currentUser.getId());
                    break;
                default:
                    log.warn("【数据权限】未知的数据权限范围：{}", dataScopeValue);
                    break;
            }

            // 将wrapper存入ThreadLocal
            DataScopeContext.setWrapper(wrapper);
            log.debug("【数据权限】数据权限条件已设置到ThreadLocal");
        } catch (Exception e) {
            log.error("【数据权限】切面执行异常：{}", e.getMessage(), e);
            // 清理ThreadLocal
            DataScopeContext.clear();
            // 抛出运行时异常，阻止访问
            throw new RuntimeException("数据权限检查失败，请稍后重试", e);
        }
    }

    /**
     * 方法执行后清理ThreadLocal
     */
    @After("@annotation(dataScope)")
    public void after(DataScope dataScope) {
        try {
            DataScopeContext.clear();
            log.debug("【数据权限】清理ThreadLocal");
        } catch (Exception e) {
            log.error("【数据权限】清理ThreadLocal异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 校验字段名是否合法（防止SQL注入）
     * 只允许字母、数字、下划线，且必须以字母或下划线开头
     */
    private boolean isValidFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        return FIELD_NAME_PATTERN.matcher(fieldName).matches();
    }

    /**
     * 构建带别名的字段名
     */
    private String buildFieldWithAlias(String alias, String field) {
        if (alias == null || alias.isEmpty()) {
            return field;
        }
        return alias + "." + field;
    }

    /**
     * 根据角色获取数据权限范围
     * 从SysRole实体中读取dataScope字段
     */
    private Integer getDataScopeByRole(SysUser user) {
        // 默认仅本人
        if (user.getRoleId() == null) {
            return 4;
        }

        // 从数据库查询角色的dataScope字段
        try {
            SysRole role = sysRoleService.getById(user.getRoleId());
            if (role != null && role.getDataScope() != null) {
                log.debug("【数据权限】从角色获取数据权限范围：roleId={}, dataScope={}", user.getRoleId(), role.getDataScope());
                return role.getDataScope();
            }
        } catch (Exception e) {
            log.error("【数据权限】获取角色数据权限失败：{}", e.getMessage(), e);
        }

        // 如果查询失败，默认仅本人
        log.warn("【数据权限】无法获取角色数据权限，默认仅本人可见");
        return 4;
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
        log.debug("【数据权限】查询用户 {} 的部门ID", user.getId());
        Long deptId = employeeMapper.selectDeptIdByUserId(user.getId());

        if (deptId == null) {
            log.warn("【数据权限】用户 {} 未关联员工信息，无法获取部门ID", user.getId());
        } else {
            log.debug("【数据权限】用户 {} 的部门ID：{}", user.getId(), deptId);
        }

        return deptId;
    }
}
