package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.dto.InsuredCompanyPageQueryDTO;
import com.whtc.employee.entity.InsuredCompany;
import com.whtc.employee.vo.InsuredCompanyVO;

public interface InsuredCompanyService extends IService<InsuredCompany> {

    /**
     * 分页查询投保公司列表（包含员工数量统计）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<InsuredCompanyVO> queryPage(InsuredCompanyPageQueryDTO queryDTO);
}
