package com.whtc.employee.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whtc.employee.common.Result;
import com.whtc.employee.dto.InsuredCompanyPageQueryDTO;
import com.whtc.employee.entity.InsuredCompany;
import com.whtc.employee.service.InsuredCompanyService;
import com.whtc.employee.vo.InsuredCompanyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/insurance-company")
@Slf4j
public class InsuredCompanyController {

    @Autowired
    private InsuredCompanyService insuredCompanyService;

    /**
     * 分页查询投保公司列表（包含员工数量统计）
     */
    @GetMapping("/page")
    public Result<Page<InsuredCompanyVO>> page(InsuredCompanyPageQueryDTO queryDTO) {
        log.info("分页查询投保公司列表，查询条件：{}", queryDTO);
        Page<InsuredCompanyVO> page = insuredCompanyService.queryPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取所有投保公司列表
     */
    @GetMapping("/list")
    public Result<List<InsuredCompany>> list() {
        log.info("获取所有投保公司列表");
        List<InsuredCompany> list = insuredCompanyService.list();
        return Result.success(list);
    }

    /**
     * 根据ID查询投保公司
     */
    @GetMapping("/{id}")
    public Result<InsuredCompany> getById(@PathVariable Long id) {
        log.info("根据id查询投保公司，id：{}", id);
        InsuredCompany company = insuredCompanyService.getById(id);
        return Result.success(company);
    }

    /**
     * 新增投保公司
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result save(@RequestBody InsuredCompany company) {
        log.info("新增投保公司：{}", company);
        insuredCompanyService.save(company);
        return Result.success();
    }

    /**
     * 更新投保公司
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result update(@RequestBody InsuredCompany company) {
        log.info("更新投保公司：{}", company);
        insuredCompanyService.updateById(company);
        return Result.success();
    }

    /**
     * 删除投保公司
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result delete(@PathVariable Long id) {
        log.info("删除投保公司，id：{}", id);
        insuredCompanyService.removeById(id);
        return Result.success();
    }
}
