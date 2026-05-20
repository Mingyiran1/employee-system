package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.entity.SysMessage;
import com.whtc.employee.mapper.SysMessageMapper;
import com.whtc.employee.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息通知服务实现
 */
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements MessageService {

    @Override
    public void sendApprovalMessage(Long userId, String title, String content, String businessType, Long businessId) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(1); // 审批通知
        message.setBusinessType(businessType);
        message.setBusinessId(businessId);
        message.setIsRead(0);

        this.save(message);
        log.info("发送审批通知：userId={}, title={}", userId, title);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getUserId, userId)
                .eq(SysMessage::getIsRead, 0);
        return this.count(wrapper);
    }

    @Override
    public PageResult getUserMessages(Long userId, Integer page, Integer size) {
        Page<SysMessage> pageInfo = new Page<>(page, size);

        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getUserId, userId)
                .orderByDesc(SysMessage::getCreateTime);

        Page<SysMessage> result = this.page(pageInfo, wrapper);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public boolean markAsRead(Long messageId, Long userId) {
        SysMessage message = this.getById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            return false;
        }
        message.setIsRead(1);
        this.updateById(message);
        return true;
    }

    @Override
    public int markAllAsRead(Long userId) {
        return baseMapper.markAllAsRead(userId);
    }
}
