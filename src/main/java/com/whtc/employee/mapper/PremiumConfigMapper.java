package com.whtc.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whtc.employee.entity.PremiumConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PremiumConfigMapper extends BaseMapper<PremiumConfig> {

    /**
     * 根据工种查询保费配置
     */
    @Select("SELECT * FROM premium_config WHERE job_type = #{jobType} AND status = 1 LIMIT 1")
    PremiumConfig selectByJobType(String jobType);
}
