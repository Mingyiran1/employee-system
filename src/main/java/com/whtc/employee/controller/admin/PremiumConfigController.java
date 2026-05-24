package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.entity.PremiumConfig;
import com.whtc.employee.service.PremiumConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/premium-config")
@Slf4j
public class PremiumConfigController {

    @Autowired
    private PremiumConfigService premiumConfigService;

    /**
     * 获取所有保费配置列表
     */
    @GetMapping("/list")
    public Result<List<PremiumConfig>> list() {
        log.info("获取所有保费配置列表");
        List<PremiumConfig> list = premiumConfigService.list();
        return Result.success(list);
    }

    /**
     * 根据ID查询保费配置
     */
    @GetMapping("/{id}")
    public Result<PremiumConfig> getById(@PathVariable Long id) {
        log.info("根据id查询保费配置，id：{}", id);
        PremiumConfig config = premiumConfigService.getById(id);
        return Result.success(config);
    }

    /**
     * 根据工种查询保费配置
     */
    @GetMapping("/job-type/{jobType}")
    public Result<PremiumConfig> getByJobType(@PathVariable String jobType) {
        log.info("根据工种查询保费配置，jobType：{}", jobType);
        PremiumConfig config = premiumConfigService.getByJobType(jobType);
        return Result.success(config);
    }

    /**
     * 新增保费配置
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result save(@RequestBody PremiumConfig config) {
        log.info("新增保费配置：{}", config);
        // 自动计算年保费
        if (config.getBaseSalary() != null && config.getRate() != null) {
            config.setAnnualPremium(config.getBaseSalary().multiply(config.getRate()));
        }
        premiumConfigService.save(config);
        return Result.success();
    }

    /**
     * 更新保费配置
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result update(@RequestBody PremiumConfig config) {
        log.info("更新保费配置：{}", config);
        // 自动计算年保费
        if (config.getBaseSalary() != null && config.getRate() != null) {
            config.setAnnualPremium(config.getBaseSalary().multiply(config.getRate()));
        }
        premiumConfigService.updateById(config);
        return Result.success();
    }

    /**
     * 删除保费配置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result delete(@PathVariable Long id) {
        log.info("删除保费配置，id：{}", id);
        premiumConfigService.removeById(id);
        return Result.success();
    }
}
