package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.entity.PremiumConfig;
import com.whtc.employee.mapper.PremiumConfigMapper;
import com.whtc.employee.service.PremiumConfigService;
import org.springframework.stereotype.Service;

@Service
public class PremiumConfigServiceImpl extends ServiceImpl<PremiumConfigMapper, PremiumConfig> implements PremiumConfigService {

    @Override
    public PremiumConfig getByJobType(String jobType) {
        return baseMapper.selectByJobType(jobType);
    }
}
