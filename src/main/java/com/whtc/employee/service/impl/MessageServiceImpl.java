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
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

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
        // 对标题和内容进行HTML转义，防止XSS攻击
        message.setTitle(HtmlUtils.htmlEscape(title));
        message.setContent(HtmlUtils.htmlEscape(content));
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

    @Override
    public int batchSendMessage(List<Long> userIds, String title, String content, Integer type) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        // 对标题和内容进行HTML转义，防止XSS攻击
        String escapedTitle = HtmlUtils.htmlEscape(title);
        String escapedContent = HtmlUtils.htmlEscape(content);

        List<SysMessage> messages = new ArrayList<>();
        for (Long userId : userIds) {
            SysMessage message = new SysMessage();
            message.setUserId(userId);
            message.setTitle(escapedTitle);
            message.setContent(escapedContent);
            message.setType(type);
            message.setIsRead(0);
            messages.add(message);
        }

        // 批量保存
        boolean success = this.saveBatch(messages);
        int count = success ? messages.size() : 0;
        log.info("批量发送消息：用户数={}, 成功数={}, title={}", userIds.size(), count, title);
        return count;
    }
}
