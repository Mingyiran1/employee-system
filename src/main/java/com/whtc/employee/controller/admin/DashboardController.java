package com.whtc.employee.controller.admin;

import com.whtc.employee.common.Result;
import com.whtc.employee.service.DashboardService;
import com.whtc.employee.vo.DashboardStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据仪表盘统计Controller
 * 提供仪表盘相关数据的REST API接口
 */
@RestController
@RequestMapping("/admin/dashboard")
@Slf4j
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取员工概览统计
     * 返回：总数、本月新增、本月离职、在职数
     */
    @GetMapping("/overview")
    public Result<DashboardStatisticsVO.EmployeeOverviewVO> getEmployeeOverview() {
        log.info("接收到获取员工概览统计请求");
        DashboardStatisticsVO.EmployeeOverviewVO overview = dashboardService.getEmployeeOverview();
        return Result.success(overview);
    }

    /**
     * 获取部门分布统计
     * 用于饼图展示
     */
    @GetMapping("/dept-distribution")
    public Result<List<DashboardStatisticsVO.DeptDistributionVO>> getDeptDistribution() {
        log.info("接收到获取部门分布统计请求");
        List<DashboardStatisticsVO.DeptDistributionVO> deptDistribution = dashboardService.getDeptDistribution();
        return Result.success(deptDistribution);
    }

    /**
     * 获取性别分布统计
     * 用于饼图展示
     */
    @GetMapping("/gender-distribution")
    public Result<DashboardStatisticsVO.GenderDistributionVO> getGenderDistribution() {
        log.info("接收到获取性别分布统计请求");
        DashboardStatisticsVO.GenderDistributionVO genderDistribution = dashboardService.getGenderDistribution();
        return Result.success(genderDistribution);
    }

    /**
     * 获取入职趋势统计（最近12个月）
     * 用于折线图展示
     */
    @GetMapping("/entry-trend")
    public Result<List<Map<String, Object>>> getEntryTrend() {
        log.info("接收到获取入职趋势统计请求");
        List<Map<String, Object>> entryTrend = dashboardService.getEntryTrend();
        return Result.success(entryTrend);
    }

    /**
     * 获取待审批数量
     */
    @GetMapping("/approval-pending")
    public Result<Long> getPendingApprovalCount() {
        log.info("接收到获取待审批数量请求");
        Long pendingCount = dashboardService.getPendingApprovalCount();
        return Result.success(pendingCount);
    }

    /**
     * 获取所有统计数据
     * 一次性返回所有仪表盘数据，方便前端一次性获取
     */
    @GetMapping("/all")
    public Result<DashboardStatisticsVO> getAllStatistics() {
        log.info("接收到获取所有仪表盘统计数据请求");
        DashboardStatisticsVO statistics = dashboardService.getAllStatistics();
        return Result.success(statistics);
    }
}
