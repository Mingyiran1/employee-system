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

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardStatisticsMapper dashboardStatisticsMapper;

    @Override
    @Cacheable(value = "dashboard:overview", key = "'all'")
    public DashboardStatisticsVO.EmployeeOverviewVO getEmployeeOverview() {
        log.info("获取员工概览统计");
        return buildEmployeeOverview();
    }

    @Override
    @Cacheable(value = "dashboard:dept", key = "'all'")
    public List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistribution() {
        log.info("获取投保公司分布统计");
        return dashboardStatisticsMapper.getDeptDistribution();
    }

    @Override
    @Cacheable(value = "dashboard:gender", key = "'all'")
    public DashboardStatisticsVO.GenderDistributionVO getGenderDistribution() {
        log.info("获取工种分布统计");
        return dashboardStatisticsMapper.getGenderDistribution();
    }

    @Override
    @Cacheable(value = "dashboard:trend", key = "'all'")
    public List<Map<String, Object>> getEntryTrend() {
        log.info("获取保险员工入职趋势统计（最近12个月）");
        return buildEntryTrend();
    }

    @Override
    @Cacheable(value = "dashboard:approval", key = "'all'")
    public Long getPendingApprovalCount() {
        log.info("获取待审批数量");
        return dashboardStatisticsMapper.getPendingApprovalCount();
    }

    @Override
    public DashboardStatisticsVO getAllStatistics() {
        log.info("获取所有仪表盘统计数据");
        DashboardStatisticsVO statistics = new DashboardStatisticsVO();
        statistics.setEmployeeOverview(buildEmployeeOverview());
        statistics.setDeptDistribution(dashboardStatisticsMapper.getDeptDistribution());
        statistics.setGenderDistribution(dashboardStatisticsMapper.getGenderDistribution());
        statistics.setEntryTrend(buildEntryTrend());
        statistics.setPendingApprovalCount(dashboardStatisticsMapper.getPendingApprovalCount());
        return statistics;
    }

    private DashboardStatisticsVO.EmployeeOverviewVO buildEmployeeOverview() {
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
        return overview;
    }

    private List<Map<String, Object>> buildEntryTrend() {
        List<Map<String, Object>> dbResult = dashboardStatisticsMapper.getEntryTrend();
        List<Map<String, Object>> filledResult = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> countMap = new HashMap<>();

        for (Map<String, Object> item : dbResult) {
            String month = (String) item.get("month");
            Long count = ((Number) item.get("count")).longValue();
            countMap.put(month, count);
        }

        for (int i = 11; i >= 0; i--) {
            String month = now.minusMonths(i).format(formatter);
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("count", countMap.getOrDefault(month, 0L));
            filledResult.add(item);
        }

        return filledResult;
    }
}
