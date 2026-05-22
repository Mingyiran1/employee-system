package com.whtc.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表导出任务实体
 */
@Data
@TableName("export_task")
public class ExportTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 状态: 0-等待中 1-执行中 2-成功 3-失败
     */
    private Integer status;

    /**
     * 总记录数
     */
    private Integer totalCount;

    /**
     * 筛选参数(JSON)
     */
    private String filterParams;

    /**
     * 导出的字段列表(JSON)
     */
    private String exportFields;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    // 状态常量
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_FAILED = 3;

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        return switch (status) {
            case STATUS_PENDING -> "等待中";
            case STATUS_RUNNING -> "执行中";
            case STATUS_SUCCESS -> "已完成";
            case STATUS_FAILED -> "失败";
            default -> "未知";
        };
    }
}
