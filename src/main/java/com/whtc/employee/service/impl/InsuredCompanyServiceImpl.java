package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.dto.InsuredCompanyPageQueryDTO;
import com.whtc.employee.entity.InsuredCompany;
import com.whtc.employee.mapper.InsuredCompanyMapper;
import com.whtc.employee.service.InsuredCompanyService;
import com.whtc.employee.vo.InsuredCompanyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InsuredCompanyServiceImpl extends ServiceImpl<InsuredCompanyMapper, InsuredCompany> implements InsuredCompanyService {

    @Autowired
    private InsuredCompanyMapper insuredCompanyMapper;

    @Override
    public Page<InsuredCompanyVO> queryPage(InsuredCompanyPageQueryDTO queryDTO) {
        Page<InsuredCompanyVO> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return insuredCompanyMapper.selectCompanyPage(page, queryDTO);
    }
}
