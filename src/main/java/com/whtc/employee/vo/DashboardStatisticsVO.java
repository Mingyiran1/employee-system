package com.whtc.employee.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 数据仪表盘统计VO
 * 封装所有仪表盘统计数据
 */
@Data
public class DashboardStatisticsVO {

    /**
     * 员工概览统计
     */
    private EmployeeOverviewVO employeeOverview;

    /**
     * 部门分布统计
     */
    private List<DeptDistributionVO> deptDistribution;

    /**
     * 性别比例统计
     */
    private GenderDistributionVO genderDistribution;

    /**
     * 入职趋势统计（最近12个月）
     */
    private List<Map<String, Object>> entryTrend;

    /**
     * 待审批数量
     */
    private Long pendingApprovalCount;

    /**
     * 员工概览统计内部类
     */
    @Data
    public static class EmployeeOverviewVO {
        /**
         * 员工总数
         */
        private Long totalCount;

        /**
         * 本月新增员工数
         */
        private Long newThisMonthCount;

        /**
         * 本月离职员工数（status=0表示离职）
         */
        private Long resignedThisMonthCount;

        /**
         * 在职员工数（status=1表示在职）
         */
        private Long activeCount;
    }

    /**
     * 部门分布统计内部类
     */
    @Data
    public static class DeptDistributionVO {
        /**
         * 部门ID
         */
        private Long deptId;

        /**
         * 部门名称
         */
        private String deptName;

        /**
         * 该部门员工数量
         */
        private Long employeeCount;
    }

    /**
     * 性别比例统计内部类
     */
    @Data
    public static class GenderDistributionVO {
        /**
         * 男员工数（gender=1）
         */
        private Long maleCount;

        /**
         * 女员工数（gender=2）
         */
        private Long femaleCount;
    }
}
