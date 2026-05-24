package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.entity.PremiumConfig;

public interface PremiumConfigService extends IService<PremiumConfig> {

    /**
     * 根据工种查询保费配置
     */
    PremiumConfig getByJobType(String jobType);
}
