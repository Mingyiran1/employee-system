package com.whtc.employee.controller.admin;

import com.whtc.employee.common.PageResult;
import com.whtc.employee.common.Result;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.entity.SysMessage;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/admin/message")
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取当前用户未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        Long count = messageService.getUnreadCount(currentUser.getId());
        return Result.success(count);
    }

    /**
     * 获取当前用户消息列表
     */
    @GetMapping("/list")
    public Result<PageResult> getMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        PageResult result = messageService.getUserMessages(currentUser.getId(), page, size);
        return Result.success(result);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read/{messageId}")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        boolean success = messageService.markAsRead(messageId, currentUser.getId());
        if (!success) {
            return Result.error("消息不存在或无权限");
        }
        return Result.success();
    }

    /**
     * 标记所有消息为已读
     */
    @PostMapping("/read-all")
    public Result<Integer> markAllAsRead() {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        int count = messageService.markAllAsRead(currentUser.getId());
        return Result.success(count);
    }
}
