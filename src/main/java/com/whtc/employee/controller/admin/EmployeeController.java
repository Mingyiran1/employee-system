package com.whtc.employee.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.whtc.employee.annotation.DataScope;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.common.Result;
import com.whtc.employee.dto.EmployeeDTO;
import com.whtc.employee.dto.EmployeePageQueryDTO;
import com.whtc.employee.entity.Employee;
import com.whtc.employee.service.EmployeeService;
import com.whtc.employee.vo.EmployeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工分页查询
     * 使用@DataScope注解进行数据权限过滤
     */
    @GetMapping("/page")
    @DataScope(deptField = "dept_id", userField = "create_by")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询，参数：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询员工
     */
    @GetMapping("/{id}")
    public Result<EmployeeVO> getById(@PathVariable Long id) {
        log.info("根据id查询员工，id：{}", id);
        EmployeeVO employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 新增员工
     */
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工，员工数据：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 更新员工
     */
    @PutMapping
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息，员工数据：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除员工，id：{}", id);
        employeeService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除员工
     */
    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestParam java.util.List<Long> ids) {
        log.info("批量删除员工，ids：{}", ids);
        employeeService.deleteByIds(ids);
        return Result.success();
    }
}
