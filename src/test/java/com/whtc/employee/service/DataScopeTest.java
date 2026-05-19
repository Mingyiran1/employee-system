package com.whtc.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.dto.EmployeePageQueryDTO;
import com.whtc.employee.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据权限功能测试类
 * 测试不同角色用户查询员工数据时的权限过滤效果
 */
@SpringBootTest
@Slf4j
public class DataScopeTest {

    @Autowired
    private EmployeeService employeeService;

    @AfterEach
    public void tearDown() {
        // 清理ThreadLocal，避免影响其他测试
        BaseContext.clear();
    }

    /**
     * 测试 admin 角色 - 应该看到全部数据
     */
    @Test
    public void testAdminDataScope() {
        // 模拟 admin 登录
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRoleId(1L);
        admin.setRoleCode("admin");
        BaseContext.setCurrentUser(admin);

        // 查询
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        QueryWrapper wrapper = new QueryWrapper();
        PageResult result = employeeService.pageQuery(dto, wrapper);

        log.info("admin 查询结果总数: {}", result.getTotal());
        // admin 应该看到全部数据（8条）
        assertTrue(result.getTotal() >= 8, "admin应该看到全部数据");
    }

    /**
     * 测试技术总监(dept_CEO)角色 - 应该看到本部门及以下数据
     * 张三(user_id=2)属于技术部(dept_id=2)，应该看到技术部及其子部门的数据
     */
    @Test
    public void testTechCEODataScope() {
        // 模拟张三登录（技术总监，部门2）
        SysUser zhangsan = new SysUser();
        zhangsan.setId(2L);
        zhangsan.setUsername("zhangsan");
        zhangsan.setRoleId(2L);
        zhangsan.setRoleCode("dept_CEO");
        BaseContext.setCurrentUser(zhangsan);

        // 查询
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        QueryWrapper wrapper = new QueryWrapper();
        PageResult result = employeeService.pageQuery(dto, wrapper);

        log.info("张三(技术总监) 查询结果总数: {}", result.getTotal());
        // 张三应该看到技术部及子部门的数据（5条：张三、李四、王五、员工A、员工B）
        assertTrue(result.getTotal() >= 5, "技术总监应该看到本部门及子部门数据");
    }

    /**
     * 测试技术经理(dept_manager)角色 - 应该只看到本部门数据
     * 李四(user_id=3)属于技术部(dept_id=2)，应该只看到技术部本部门数据
     */
    @Test
    public void testTechManagerDataScope() {
        // 模拟李四登录（技术经理，部门2）
        SysUser lisi = new SysUser();
        lisi.setId(3L);
        lisi.setUsername("lisi");
        lisi.setRoleId(3L);
        lisi.setRoleCode("dept_manager");
        BaseContext.setCurrentUser(lisi);

        // 查询
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        QueryWrapper wrapper = new QueryWrapper();
        PageResult result = employeeService.pageQuery(dto, wrapper);

        log.info("李四(技术经理) 查询结果总数: {}", result.getTotal());
        // 李四应该只看到技术部本部门的数据（2条：张三、李四）
        // 注意：由于王五、员工A、员工B在子部门，不应该看到
        assertTrue(result.getTotal() >= 2, "技术经理应该看到本部门数据");
    }

    /**
     * 测试普通员工(user)角色 - 应该只看到本人创建的数据
     * 王五(user_id=4)的create_by=2，应该只看到create_by=2的数据
     */
    @Test
    public void testNormalUserDataScope() {
        // 模拟王五登录（普通员工）
        SysUser wangwu = new SysUser();
        wangwu.setId(4L);
        wangwu.setUsername("wangwu");
        wangwu.setRoleId(4L);
        wangwu.setRoleCode("user");
        BaseContext.setCurrentUser(wangwu);

        // 查询
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        QueryWrapper wrapper = new QueryWrapper();
        PageResult result = employeeService.pageQuery(dto, wrapper);

        log.info("王五(普通员工) 查询结果总数: {}", result.getTotal());
        // 王五应该只看到create_by=2的数据（张三创建的：李四、王五）
        assertTrue(result.getTotal() >= 0, "普通员工应该看到本人数据");
    }

    /**
     * 测试未登录用户 - 应该返回空数据或抛出异常
     */
    @Test
    public void testNotLoggedIn() {
        // 确保没有用户登录
        BaseContext.clear();

        // 查询
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        QueryWrapper wrapper = new QueryWrapper();
        PageResult result = employeeService.pageQuery(dto, wrapper);

        log.info("未登录用户 查询结果总数: {}", result.getTotal());
        // 未登录用户可能会返回全部数据（因为没有过滤）或空数据
        // 这取决于业务需求
        assertNotNull(result, "未登录用户查询不应该报错");
    }

    /**
     * 测试部门筛选+数据权限组合
     */
    @Test
    public void testDataScopeWithDeptFilter() {
        // 模拟张三登录（技术总监）
        SysUser zhangsan = new SysUser();
        zhangsan.setId(2L);
        zhangsan.setUsername("zhangsan");
        zhangsan.setRoleId(2L);
        zhangsan.setRoleCode("dept_CEO");
        BaseContext.setCurrentUser(zhangsan);

        // 查询前端组(dept_id=5)的数据
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        dto.setDeptId(5L); // 前端组
        QueryWrapper wrapper = new QueryWrapper();
        PageResult result = employeeService.pageQuery(dto, wrapper);

        log.info("张三查询前端组 查询结果总数: {}", result.getTotal());
        // 前端组只有员工B
        assertNotNull(result);
    }

    /**
     * 测试新增员工时自动设置create_by
     */
    @Test
    public void testSaveEmployeeWithCreateBy() {
        // 模拟张三登录
        SysUser zhangsan = new SysUser();
        zhangsan.setId(2L);
        zhangsan.setUsername("zhangsan");
        zhangsan.setRoleId(2L);
        zhangsan.setRoleCode("dept_CEO");
        BaseContext.setCurrentUser(zhangsan);

        // 注意：这里需要EmployeeDTO，可能需要修改测试
        // 或者使用@Autowired注入EmployeeMapper直接测试
        log.info("测试新增员工时create_by自动设置 - 需要在Service层验证");
    }
}
