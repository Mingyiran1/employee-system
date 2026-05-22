package com.whtc.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whtc.employee.entity.ExportTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 导出任务 Mapper
 */
@Mapper
public interface ExportTaskMapper extends BaseMapper<ExportTask> {

    /**
     * 查询用户的导出任务列表
     */
    @Select("SELECT * FROM export_task WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 20")
    List<ExportTask> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询待处理的任务
     */
    @Select("SELECT * FROM export_task WHERE status = 0 ORDER BY create_time ASC LIMIT 10")
    List<ExportTask> selectPendingTasks();

    /**
     * 清理7天前的已完成任务
     */
    @Select("DELETE FROM export_task WHERE status IN (2, 3) AND create_time < DATE_SUB(NOW(), INTERVAL 7 DAY)")
    void cleanOldTasks();
}
