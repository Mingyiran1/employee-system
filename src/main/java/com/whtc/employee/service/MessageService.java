package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.entity.SysMessage;

/**
 * 消息通知服务接口
 */
public interface MessageService extends IService<SysMessage> {

    /**
     * 发送审批通知消息
     * @param userId 接收人ID
     * @param title 标题
     * @param content 内容
     * @param businessType 业务类型
     * @param businessId 业务ID
     */
    void sendApprovalMessage(Long userId, String title, String content, String businessType, Long businessId);

    /**
     * 获取用户未读消息数量
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 获取用户消息列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult getUserMessages(Long userId, Integer page, Integer size);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 当前用户ID（用于权限校验）
     * @return 是否成功
     */
    boolean markAsRead(Long messageId, Long userId);

    /**
     * 标记用户所有消息为已读
     * @param userId 用户ID
     * @return 更新的条数
     */
    int markAllAsRead(Long userId);
}
