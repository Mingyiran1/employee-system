package com.whtc.employee.service.impl;

import com.whtc.employee.context.BaseContext;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.mapper.DashboardStatisticsMapper;
import com.whtc.employee.mapper.DepartmentMapper;
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

    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 获取当前用户的数据权限范围
     * 1-全部 2-本部门及以下 3-本部门 4-仅本人
     */
    private Integer getCurrentUserDataScope() {
        SysUser currentUser = BaseContext.getCurrentUser();
        log.info("获取数据权限 - 当前用户: {}, roleId: {}",
                currentUser != null ? currentUser.getUsername() : "null",
                currentUser != null ? currentUser.getRoleId() : "null");

        if (currentUser == null || currentUser.getRoleId() == null) {
            log.warn("用户或roleId为空，默认仅本人权限");
            return 4; // 默认仅本人
        }
        return switch (currentUser.getRoleId().intValue()) {
            case 1 -> 1; // admin - 全部
            case 2 -> 2; // dept_CEO - 本部门及以下
            case 3 -> 3; // dept_manager - 本部门
            case 4 -> 4; // user - 仅本人
            default -> 4;
        };
    }

    /**
     * 获取当前用户所属部门ID
     */
    private Long getCurrentUserDeptId() {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        return dashboardStatisticsMapper.getUserDeptId(currentUser.getId());
    }

    /**
     * 获取当前用户可查看的部门ID列表
     */
    private List<Long> getCurrentUserDeptIds() {
        Integer dataScope = getCurrentUserDataScope();
        Long currentUserId = BaseContext.getCurrentUserId();

        // 全部数据权限，返回null表示不限制
        if (dataScope == 1) {
            return null;
        }

        // 仅本人权限，返回空列表
        if (dataScope == 4) {
            return new ArrayList<>();
        }

        Long deptId = getCurrentUserDeptId();
        if (deptId == null) {
            return new ArrayList<>();
        }

        // 本部门权限
        if (dataScope == 3) {
            List<Long> deptIds = new ArrayList<>();
            deptIds.add(deptId);
            return deptIds;
        }

        // 本部门及以下权限
        if (dataScope == 2) {
            List<Long> deptIds = departmentMapper.selectChildDeptIds(deptId);
            deptIds.add(deptId);
            return deptIds;
        }

        return new ArrayList<>();
    }

    /**
     * 生成带数据权限标识的缓存key
     */
    private String generateCacheKey(String baseKey) {
        Integer dataScope = getCurrentUserDataScope();

        // 全部数据权限，不需要额外标识
        if (dataScope == 1) {
            return baseKey;
        }

        // 其他权限，加入用户ID和数据范围标识
        Long userId = BaseContext.getCurrentUserId();
        return baseKey + ":user_" + userId + ":scope_" + dataScope;
    }

    @Override
    @Cacheable(value = "dashboard:overview", key = "#root.target.generateCacheKey('employeeOverview')")
    public DashboardStatisticsVO.EmployeeOverviewVO getEmployeeOverview() {
        log.info("获取员工概览统计");

        // 获取当前用户的数据权限
        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();
        Long userId = BaseContext.getCurrentUserId();

        // 获取本月开始和结束时间
        LocalDateTime startOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .atStartOfDay();

        DashboardStatisticsVO.EmployeeOverviewVO overview = new DashboardStatisticsVO.EmployeeOverviewVO();

        // 根据数据权限查询统计
        if (dataScope == 4 && userId != null) {
            // 仅本人权限
            overview.setTotalCount(dashboardStatisticsMapper.getTotalEmployeeCountByUserId(userId));
            overview.setNewThisMonthCount(dashboardStatisticsMapper.getNewEmployeeThisMonthCountByUserId(startOfMonth, endOfMonth, userId));
            overview.setResignedThisMonthCount(dashboardStatisticsMapper.getResignedEmployeeThisMonthCountByUserId(startOfMonth, endOfMonth, userId));
            overview.setActiveCount(dashboardStatisticsMapper.getActiveEmployeeCountByUserId(userId));
        } else if (deptIds != null && !deptIds.isEmpty()) {
            // 部门权限范围
            overview.setTotalCount(dashboardStatisticsMapper.getTotalEmployeeCountByDeptIds(deptIds));
            overview.setNewThisMonthCount(dashboardStatisticsMapper.getNewEmployeeThisMonthCountByDeptIds(startOfMonth, endOfMonth, deptIds));
            overview.setResignedThisMonthCount(dashboardStatisticsMapper.getResignedEmployeeThisMonthCountByDeptIds(startOfMonth, endOfMonth, deptIds));
            overview.setActiveCount(dashboardStatisticsMapper.getActiveEmployeeCountByDeptIds(deptIds));
        } else if (dataScope == 1) {
            // 全部数据权限
            overview.setTotalCount(dashboardStatisticsMapper.getTotalEmployeeCount());
            overview.setNewThisMonthCount(dashboardStatisticsMapper.getNewEmployeeThisMonthCount(startOfMonth, endOfMonth));
            overview.setResignedThisMonthCount(dashboardStatisticsMapper.getResignedEmployeeThisMonthCount(startOfMonth, endOfMonth));
            overview.setActiveCount(dashboardStatisticsMapper.getActiveEmployeeCount());
        } else {
            // 无权限，返回0
            overview.setTotalCount(0L);
            overview.setNewThisMonthCount(0L);
            overview.setResignedThisMonthCount(0L);
            overview.setActiveCount(0L);
        }

        log.info("员工概览统计结果：总数={}, 本月新增={}, 本月离职={}, 在职数={}",
                overview.getTotalCount(),
                overview.getNewThisMonthCount(),
                overview.getResignedThisMonthCount(),
                overview.getActiveCount());

        return overview;
    }

    @Override
    @Cacheable(value = "dashboard:dept", key = "#root.target.generateCacheKey('deptDistribution')")
    public List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistribution() {
        log.info("获取部门分布统计");

        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();
        List<DashboardStatisticsVO.DeptDistributionVO> result;

        if (dataScope == 4) {
            // 仅本人权限，返回空列表
            result = new ArrayList<>();
        } else if (deptIds != null && !deptIds.isEmpty()) {
            // 部门权限范围
            result = dashboardStatisticsMapper.getDeptDistributionByDeptIds(deptIds);
        } else if (dataScope == 1) {
            // 全部数据权限
            result = dashboardStatisticsMapper.getDeptDistribution();
        } else {
            result = new ArrayList<>();
        }

        log.info("部门分布统计结果：{}个部门", result.size());
        return result;
    }

    @Override
    @Cacheable(value = "dashboard:gender", key = "#root.target.generateCacheKey('genderDistribution')")
    public DashboardStatisticsVO.GenderDistributionVO getGenderDistribution() {
        log.info("获取性别分布统计");

        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();
        Long userId = BaseContext.getCurrentUserId();
        DashboardStatisticsVO.GenderDistributionVO result;

        if (dataScope == 4 && userId != null) {
            // 仅本人权限
            result = dashboardStatisticsMapper.getGenderDistributionByUserId(userId);
        } else if (deptIds != null && !deptIds.isEmpty()) {
            // 部门权限范围
            result = dashboardStatisticsMapper.getGenderDistributionByDeptIds(deptIds);
        } else if (dataScope == 1) {
            // 全部数据权限
            result = dashboardStatisticsMapper.getGenderDistribution();
        } else {
            // 无权限，返回空统计
            result = new DashboardStatisticsVO.GenderDistributionVO();
            result.setMaleCount(0L);
            result.setFemaleCount(0L);
            result.setUnknownCount(0L);
        }

        // 调试：打印原始SQL查询结果
        log.info("性别分布原始数据：男={}, 女={}, 未知={}",
                result.getMaleCount(), result.getFemaleCount(), result.getUnknownCount());
        return result;
    }

    @Override
    @Cacheable(value = "dashboard:trend", key = "#root.target.generateCacheKey('entryTrend')")
    public List<Map<String, Object>> getEntryTrend() {
        log.info("获取入职趋势统计（最近12个月）");

        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();
        Long userId = BaseContext.getCurrentUserId();

        // 1. 从数据库获取有入职记录的数据
        List<Map<String, Object>> dbResult;
        if (dataScope == 4 && userId != null) {
            dbResult = dashboardStatisticsMapper.getEntryTrendByUserId(userId);
        } else if (deptIds != null && !deptIds.isEmpty()) {
            dbResult = dashboardStatisticsMapper.getEntryTrendByDeptIds(deptIds);
        } else if (dataScope == 1) {
            dbResult = dashboardStatisticsMapper.getEntryTrend();
        } else {
            dbResult = new ArrayList<>();
        }
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
    @Cacheable(value = "dashboard:approval", key = "#root.target.generateCacheKey('pendingApprovalCount')")
    public Long getPendingApprovalCount() {
        log.info("获取待审批数量");

        Long userId = BaseContext.getCurrentUserId();
        Integer dataScope = getCurrentUserDataScope();
        Long result;

        if (userId == null) {
            result = 0L;
        } else if (dataScope == 1) {
            // 管理员查看所有待审批
            result = dashboardStatisticsMapper.getPendingApprovalCount();
        } else {
            // 其他用户只能查看与自己相关的待审批
            result = dashboardStatisticsMapper.getPendingApprovalCountByUserId(userId);
        }

        log.info("待审批数量：{}", result);
        return result;
    }

    @Override
    public DashboardStatisticsVO getAllStatistics() {
        log.info("获取所有仪表盘统计数据");

        DashboardStatisticsVO statistics = new DashboardStatisticsVO();
        statistics.setEmployeeOverview(getEmployeeOverviewDirect());
        statistics.setDeptDistribution(getDeptDistributionDirect());
        statistics.setGenderDistribution(getGenderDistributionDirect());
        statistics.setEntryTrend(getEntryTrendDirect());
        statistics.setPendingApprovalCount(getPendingApprovalCountDirect());

        log.info("所有仪表盘统计数据获取完成");
        return statistics;
    }

    // 直接查询方法（供 getAllStatistics 使用，避免缓存代理问题）
    private DashboardStatisticsVO.EmployeeOverviewVO getEmployeeOverviewDirect() {
        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();
        Long userId = BaseContext.getCurrentUserId();

        LocalDateTime startOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .atStartOfDay();

        DashboardStatisticsVO.EmployeeOverviewVO overview = new DashboardStatisticsVO.EmployeeOverviewVO();

        if (dataScope == 4 && userId != null) {
            overview.setTotalCount(dashboardStatisticsMapper.getTotalEmployeeCountByUserId(userId));
            overview.setNewThisMonthCount(dashboardStatisticsMapper.getNewEmployeeThisMonthCountByUserId(startOfMonth, endOfMonth, userId));
            overview.setResignedThisMonthCount(dashboardStatisticsMapper.getResignedEmployeeThisMonthCountByUserId(startOfMonth, endOfMonth, userId));
            overview.setActiveCount(dashboardStatisticsMapper.getActiveEmployeeCountByUserId(userId));
        } else if (deptIds != null && !deptIds.isEmpty()) {
            overview.setTotalCount(dashboardStatisticsMapper.getTotalEmployeeCountByDeptIds(deptIds));
            overview.setNewThisMonthCount(dashboardStatisticsMapper.getNewEmployeeThisMonthCountByDeptIds(startOfMonth, endOfMonth, deptIds));
            overview.setResignedThisMonthCount(dashboardStatisticsMapper.getResignedEmployeeThisMonthCountByDeptIds(startOfMonth, endOfMonth, deptIds));
            overview.setActiveCount(dashboardStatisticsMapper.getActiveEmployeeCountByDeptIds(deptIds));
        } else if (dataScope == 1) {
            overview.setTotalCount(dashboardStatisticsMapper.getTotalEmployeeCount());
            overview.setNewThisMonthCount(dashboardStatisticsMapper.getNewEmployeeThisMonthCount(startOfMonth, endOfMonth));
            overview.setResignedThisMonthCount(dashboardStatisticsMapper.getResignedEmployeeThisMonthCount(startOfMonth, endOfMonth));
            overview.setActiveCount(dashboardStatisticsMapper.getActiveEmployeeCount());
        } else {
            overview.setTotalCount(0L);
            overview.setNewThisMonthCount(0L);
            overview.setResignedThisMonthCount(0L);
            overview.setActiveCount(0L);
        }

        return overview;
    }

    private List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistributionDirect() {
        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();

        if (dataScope == 4) {
            return new ArrayList<>();
        } else if (deptIds != null && !deptIds.isEmpty()) {
            return dashboardStatisticsMapper.getDeptDistributionByDeptIds(deptIds);
        } else if (dataScope == 1) {
            return dashboardStatisticsMapper.getDeptDistribution();
        }
        return new ArrayList<>();
    }

    private DashboardStatisticsVO.GenderDistributionVO getGenderDistributionDirect() {
        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();
        Long userId = BaseContext.getCurrentUserId();
        DashboardStatisticsVO.GenderDistributionVO result;

        if (dataScope == 4 && userId != null) {
            result = dashboardStatisticsMapper.getGenderDistributionByUserId(userId);
        } else if (deptIds != null && !deptIds.isEmpty()) {
            result = dashboardStatisticsMapper.getGenderDistributionByDeptIds(deptIds);
        } else if (dataScope == 1) {
            result = dashboardStatisticsMapper.getGenderDistribution();
        } else {
            result = new DashboardStatisticsVO.GenderDistributionVO();
            result.setMaleCount(0L);
            result.setFemaleCount(0L);
            result.setUnknownCount(0L);
        }

        return result;
    }

    private List<Map<String, Object>> getEntryTrendDirect() {
        List<Long> deptIds = getCurrentUserDeptIds();
        Integer dataScope = getCurrentUserDataScope();
        Long userId = BaseContext.getCurrentUserId();

        List<Map<String, Object>> dbResult;
        if (dataScope == 4 && userId != null) {
            dbResult = dashboardStatisticsMapper.getEntryTrendByUserId(userId);
        } else if (deptIds != null && !deptIds.isEmpty()) {
            dbResult = dashboardStatisticsMapper.getEntryTrendByDeptIds(deptIds);
        } else if (dataScope == 1) {
            dbResult = dashboardStatisticsMapper.getEntryTrend();
        } else {
            dbResult = new ArrayList<>();
        }

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

    private Long getPendingApprovalCountDirect() {
        Long userId = BaseContext.getCurrentUserId();
        Integer dataScope = getCurrentUserDataScope();

        if (userId == null) {
            return 0L;
        } else if (dataScope == 1) {
            return dashboardStatisticsMapper.getPendingApprovalCount();
        } else {
            return dashboardStatisticsMapper.getPendingApprovalCountByUserId(userId);
        }
    }
}
