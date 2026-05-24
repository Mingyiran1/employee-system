package com.whtc.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whtc.employee.dto.InsuredCompanyPageQueryDTO;
import com.whtc.employee.entity.InsuredCompany;
import com.whtc.employee.vo.InsuredCompanyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InsuredCompanyMapper extends BaseMapper<InsuredCompany> {

    /**
     * 分页查询投保公司列表（包含员工数量统计）
     */
    Page<InsuredCompanyVO> selectCompanyPage(Page<InsuredCompanyVO> page,
                                              @Param("query") InsuredCompanyPageQueryDTO queryDTO);
}
