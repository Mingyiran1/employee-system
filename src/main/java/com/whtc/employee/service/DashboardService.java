package com.whtc.employee.service;

import com.whtc.employee.vo.DashboardStatisticsVO;

import java.util.List;
import java.util.Map;

/**
 * 数据仪表盘统计Service接口
 */
public interface DashboardService {

    /**
     * 获取员工概览统计
     *
     * @return 员工概览VO
     */
    DashboardStatisticsVO.EmployeeOverviewVO getEmployeeOverview();

    /**
     * 获取部门分布统计
     *
     * @return 部门分布列表
     */
    List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistribution();

    /**
     * 获取性别分布统计
     *
     * @return 性别分布VO
     */
    DashboardStatisticsVO.GenderDistributionVO getGenderDistribution();

    /**
     * 获取入职趋势统计（最近12个月）
     *
     * @return 入职趋势列表
     */
    List<Map<String, Object>> getEntryTrend();

    /**
     * 获取待审批数量
     *
     * @return 待审批数量
     */
    Long getPendingApprovalCount();

    /**
     * 获取所有统计数据
     *
     * @return 仪表盘统计VO
     */
    DashboardStatisticsVO getAllStatistics();
}
