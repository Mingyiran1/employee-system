package com.whtc.employee.mapper;

import com.whtc.employee.vo.DashboardStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据仪表盘统计Mapper
 * 提供各类统计数据查询
 * SQL定义在 resources/mapper/DashboardStatisticsMapper.xml
 */
@Mapper
public interface DashboardStatisticsMapper {

    // ==================== 基础统计（全部数据权限）====================

    /**
     * 获取员工总数
     */
    Long getTotalEmployeeCount();

    /**
     * 获取本月新增员工数（根据create_time）
     */
    Long getNewEmployeeThisMonthCount(@Param("startOfMonth") LocalDateTime startOfMonth,
                                      @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * 获取本月离职员工数（基于审批流程记录）
     * 使用 approval_record 表中 type=3（离职）且 status=1（已通过）的记录
     * 根据审批通过时间(create_time)判断本月离职
     */
    Long getResignedEmployeeThisMonthCount(@Param("startOfMonth") LocalDateTime startOfMonth,
                                           @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * 获取在职员工数（status=1表示在职）
     */
    Long getActiveEmployeeCount();

    /**
     * 获取部门分布统计
     * 返回每个部门的人数
     */
    List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistribution();

    /**
     * 获取性别比例统计
     * gender=1为男，gender=0或2为女，gender为null或其他值为未知
     */
    DashboardStatisticsVO.GenderDistributionVO getGenderDistribution();

    /**
     * 获取入职趋势统计（最近12个月，每个月的入职人数）
     * 返回入职趋势列表，格式：月份和人数
     */
    List<Map<String, Object>> getEntryTrend();

    /**
     * 获取待审批的审批记录数
     */
    Long getPendingApprovalCount();

    // ==================== 按部门ID列表统计（部门权限）====================

    /**
     * 获取指定部门的员工总数
     */
    Long getTotalEmployeeCountByDeptIds(@Param("deptIds") List<Long> deptIds);

    /**
     * 获取指定部门本月新增员工数
     */
    Long getNewEmployeeThisMonthCountByDeptIds(@Param("startOfMonth") LocalDateTime startOfMonth,
                                               @Param("endOfMonth") LocalDateTime endOfMonth,
                                               @Param("deptIds") List<Long> deptIds);

    /**
     * 获取指定部门本月离职员工数（基于审批流程记录）
     */
    Long getResignedEmployeeThisMonthCountByDeptIds(@Param("startOfMonth") LocalDateTime startOfMonth,
                                                    @Param("endOfMonth") LocalDateTime endOfMonth,
                                                    @Param("deptIds") List<Long> deptIds);

    /**
     * 获取指定部门在职员工数
     */
    Long getActiveEmployeeCountByDeptIds(@Param("deptIds") List<Long> deptIds);

    /**
     * 获取指定部门的部门分布统计
     */
    List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistributionByDeptIds(@Param("deptIds") List<Long> deptIds);

    /**
     * 获取指定部门的性别比例统计
     */
    DashboardStatisticsVO.GenderDistributionVO getGenderDistributionByDeptIds(@Param("deptIds") List<Long> deptIds);

    /**
     * 获取指定部门的入职趋势统计
     */
    List<Map<String, Object>> getEntryTrendByDeptIds(@Param("deptIds") List<Long> deptIds);

    // ==================== 按用户ID统计（仅本人权限）====================

    /**
     * 获取指定用户创建的员工总数
     */
    Long getTotalEmployeeCountByUserId(@Param("userId") Long userId);

    /**
     * 获取指定用户本月新增员工数
     */
    Long getNewEmployeeThisMonthCountByUserId(@Param("startOfMonth") LocalDateTime startOfMonth,
                                              @Param("endOfMonth") LocalDateTime endOfMonth,
                                              @Param("userId") Long userId);

    /**
     * 获取指定用户创建的员工本月离职数（基于审批流程记录）
     */
    Long getResignedEmployeeThisMonthCountByUserId(@Param("startOfMonth") LocalDateTime startOfMonth,
                                                   @Param("endOfMonth") LocalDateTime endOfMonth,
                                                   @Param("userId") Long userId);

    /**
     * 获取指定用户创建的在职员工数
     */
    Long getActiveEmployeeCountByUserId(@Param("userId") Long userId);

    /**
     * 获取指定用户创建的员工性别比例统计
     */
    DashboardStatisticsVO.GenderDistributionVO getGenderDistributionByUserId(@Param("userId") Long userId);

    /**
     * 获取指定用户创建的员工入职趋势统计
     */
    List<Map<String, Object>> getEntryTrendByUserId(@Param("userId") Long userId);

    /**
     * 获取与用户相关的待审批数量
     */
    Long getPendingApprovalCountByUserId(@Param("userId") Long userId);

    // ==================== 辅助查询方法 ====================

    /**
     * 获取用户所属部门ID
     */
    Long getUserDeptId(@Param("userId") Long userId);

    /**
     * 调试：检查entry_date为NULL的记录数
     */
    Long getNullEntryDateCount();

    /**
     * 调试：获取所有entry_date值
     */
    List<String> getSampleEntryDates();
}
