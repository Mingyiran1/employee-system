package com.whtc.employee.mapper;

import com.whtc.employee.vo.DashboardStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据仪表盘统计Mapper
 * 提供各类统计数据查询
 */
@Mapper
public interface DashboardStatisticsMapper {

    /**
     * 获取员工总数
     *
     * @return 员工总数
     */
    @Select("SELECT COUNT(*) FROM employee WHERE is_deleted = 0")
    Long getTotalEmployeeCount();

    /**
     * 获取本月新增员工数（根据create_time）
     *
     * @param startOfMonth 本月开始时间
     * @param endOfMonth   本月结束时间
     * @return 本月新增员工数
     */
    @Select("SELECT COUNT(*) FROM employee WHERE is_deleted = 0 " +
            "AND create_time >= #{startOfMonth} AND create_time < #{endOfMonth}")
    Long getNewEmployeeThisMonthCount(@Param("startOfMonth") LocalDateTime startOfMonth,
                                      @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * 获取本月离职员工数（status=0表示离职）
     * 根据update_time判断本月离职
     *
     * @param startOfMonth 本月开始时间
     * @param endOfMonth   本月结束时间
     * @return 本月离职员工数
     */
    @Select("SELECT COUNT(*) FROM employee WHERE is_deleted = 0 AND status = 0 " +
            "AND update_time >= #{startOfMonth} AND update_time < #{endOfMonth}")
    Long getResignedEmployeeThisMonthCount(@Param("startOfMonth") LocalDateTime startOfMonth,
                                           @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * 获取在职员工数（status=1表示在职）
     *
     * @return 在职员工数
     */
    @Select("SELECT COUNT(*) FROM employee WHERE is_deleted = 0 AND status = 1")
    Long getActiveEmployeeCount();

    /**
     * 获取部门分布统计
     * 返回每个部门的人数
     *
     * @return 部门分布列表
     */
    @Select("SELECT d.id as deptId, d.name as deptName, COUNT(e.id) as employeeCount " +
            "FROM department d " +
            "LEFT JOIN employee e ON d.id = e.dept_id AND e.is_deleted = 0 " +
            "WHERE d.is_deleted = 0 " +
            "GROUP BY d.id, d.name " +
            "ORDER BY employeeCount DESC")
    List<DashboardStatisticsVO.DeptDistributionVO> getDeptDistribution();

    /**
     * 获取性别比例统计
     * gender=1为男，gender=0或2为女（0是前端表单使用的值）
     *
     * @return 性别分布VO
     */
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN gender = 1 THEN 1 ELSE 0 END), 0) as maleCount, " +
            "COALESCE(SUM(CASE WHEN gender = 0 OR gender = 2 THEN 1 ELSE 0 END), 0) as femaleCount " +
            "FROM employee WHERE is_deleted = 0")
    DashboardStatisticsVO.GenderDistributionVO getGenderDistribution();

    /**
     * 获取入职趋势统计（最近12个月，每个月的入职人数）
     *
     * @return 入职趋势列表，格式：月份和人数
     */
    @Select("SELECT DATE_FORMAT(entry_date, '%Y-%m') as month, COUNT(*) as count " +
            "FROM employee WHERE is_deleted = 0 " +
            "AND entry_date IS NOT NULL " +
            "AND entry_date >= DATE_SUB(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL 11 MONTH) " +
            "GROUP BY DATE_FORMAT(entry_date, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> getEntryTrend();

    /**
     * 调试：检查entry_date为NULL的记录数
     */
    @Select("SELECT COUNT(*) FROM employee WHERE is_deleted = 0 AND entry_date IS NULL")
    Long getNullEntryDateCount();

    /**
     * 调试：获取所有entry_date值
     */
    @Select("SELECT entry_date FROM employee WHERE is_deleted = 0 LIMIT 5")
    List<String> getSampleEntryDates();

    /**
     * 获取待审批的审批记录数
     *
     * @return 待审批数量
     */
    @Select("SELECT COUNT(*) FROM approval_record WHERE approval_status = 0 AND is_deleted = 0")
    Long getPendingApprovalCount();
}
