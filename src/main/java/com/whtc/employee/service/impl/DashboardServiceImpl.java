package com.whtc.employee.service.impl;

import com.whtc.employee.mapper.DashboardStatisticsMapper;
import com.whtc.employee.service.DashboardService;
import com.whtc.employee.vo.DashboardStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据仪表盘统计Service实现类
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardStatisticsMapper dashboardStatisticsMapper;

    @Override
    @Cacheable(value = "dashboard:overview", key = "'employeeOverview'")
    public DashboardStatisticsVO.EmployeeOverviewVO getEmployeeOverview() {
        log.info("获取员工概览统计");

        // 获取本月开始和结束时间
        LocalDateTime startOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .atStartOfDay();

        DashboardStatisticsVO.EmployeeOverviewVO overview = new DashboardStatisticsVO.EmployeeOverviewVO();
        overview.setTotalCount(dashboardStatisticsMapper.getTotalEmployeeCount());
        overview.setNewThisMonthCount(dashboardStatisticsMapper.getNewEmployeeThisMonthCount(startOfMonth, endOfMonth));
        overview.setResignedThisMonthCount(dashboardStatisticsMapper.getResignedEmployeeThisMonthCount(startOfMonth, endOfMonth));
        overview.setActiveCount(dashboardStatisticsMapper.getActiveEmployeeCount());

        log.info("员工概览统计结果：总数={}, 本月新增={}, 本月离职={}, 在职数={}",
                overview.getTotalCount(),
                overview.getNewThisMonthCount(),
                overview.getResignedThisMonthCount(),
                overview.getActiveCount());

        return overview;
    }

    @Override
    @Cacheable(value = "dashboard:dept", key = "'deptDistribution'")
    public List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistribution() {
        log.info("获取部门分布统计");
        List<DashboardStatisticsVO.DeptDistributionVO> result = dashboardStatisticsMapper.getDeptDistribution();
        log.info("部门分布统计结果：{}个部门", result.size());
        return result;
    }

    @Override
    @Cacheable(value = "dashboard:gender", key = "'genderDistribution'")
    public DashboardStatisticsVO.GenderDistributionVO getGenderDistribution() {
        log.info("获取性别分布统计");
        DashboardStatisticsVO.GenderDistributionVO result = dashboardStatisticsMapper.getGenderDistribution();
        // 调试：打印原始SQL查询结果
        log.info("性别分布原始数据：男={}, 女={}", result.getMaleCount(), result.getFemaleCount());
        return result;
    }

    @Override
    @Cacheable(value = "dashboard:trend", key = "'entryTrend'")
    public List<Map<String, Object>> getEntryTrend() {
        log.info("获取入职趋势统计（最近12个月）");

        // 1. 从数据库获取有入职记录的数据
        List<Map<String, Object>> dbResult = dashboardStatisticsMapper.getEntryTrend();
        log.info("数据库查询结果：{}个月有数据", dbResult.size());

        // 2. 生成最近12个月的月份列表（包含当前月）
        List<Map<String, Object>> filledResult = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 将数据库结果转换为Map，方便查找
        Map<String, Long> countMap = new HashMap<>();
        for (Map<String, Object> item : dbResult) {
            String month = (String) item.get("month");
            Long count = ((Number) item.get("count")).longValue();
            countMap.put(month, count);
        }

        // 3. 填充最近12个月的数据（包括缺失的月份，count设为0）
        for (int i = 11; i >= 0; i--) {
            String month = now.minusMonths(i).format(formatter);
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("count", countMap.getOrDefault(month, 0L));
            filledResult.add(item);
        }

        log.info("入职趋势统计结果：{}个月的数据（已填充缺失月份）", filledResult.size());
        return filledResult;
    }

    @Override
    @Cacheable(value = "dashboard:approval", key = "'pendingApprovalCount'")
    public Long getPendingApprovalCount() {
        log.info("获取待审批数量");
        Long result = dashboardStatisticsMapper.getPendingApprovalCount();
        log.info("待审批数量：{}", result);
        return result;
    }

    @Override
    @Cacheable(value = "dashboard:all", key = "'allStatistics'")
    public DashboardStatisticsVO getAllStatistics() {
        log.info("获取所有仪表盘统计数据");

        DashboardStatisticsVO statistics = new DashboardStatisticsVO();
        statistics.setEmployeeOverview(getEmployeeOverview());
        statistics.setDeptDistribution(getDeptDistribution());
        statistics.setGenderDistribution(getGenderDistribution());
        statistics.setEntryTrend(getEntryTrend());
        statistics.setPendingApprovalCount(getPendingApprovalCount());

        log.info("所有仪表盘统计数据获取完成");
        return statistics;
    }
}
