package com.whtc.employee.controller.admin;

import com.whtc.employee.common.PageResult;
import com.whtc.employee.common.Result;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.dto.ApprovalProcessDTO;
import com.whtc.employee.entity.ApprovalRecord;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.service.ApprovalService;
import com.whtc.employee.vo.ApprovalDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 审批流程管理控制器
 */
@RestController
@RequestMapping("/admin/approval")
@Slf4j
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    /**
     * 发起审批
     * @param dto 审批信息
     * @return 审批记录ID
     */
    @PostMapping("/start")
    public Result<Long> startApproval(@RequestBody ApprovalProcessDTO dto) {
        log.info("发起审批：businessType={}, businessId={}", dto.getBusinessType(), dto.getBusinessId());

        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            Long recordId = approvalService.startApproval(
                    dto.getBusinessType(),
                    dto.getBusinessId(),
                    currentUser.getId()
            );
            return Result.success(recordId);
        } catch (Exception e) {
            log.error("发起审批失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批处理（通过/拒绝）
     * @param recordId 审批记录ID
     * @param dto 审批结果
     * @return 处理结果
     */
    @PostMapping("/process/{recordId}")
    public Result<Void> processApproval(
            @PathVariable Long recordId,
            @RequestBody ApprovalProcessDTO dto) {
        log.info("审批处理：recordId={}, status={}", recordId, dto.getApprovalStatus());

        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            approvalService.processApproval(
                    recordId,
                    currentUser.getId(),
                    dto.getApprovalStatus(),
                    dto.getComment()
            );
            return Result.success();
        } catch (Exception e) {
            log.error("审批处理失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待审批列表
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping("/pending")
    public Result<PageResult> getPendingApprovals(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        PageResult result = approvalService.getPendingApprovals(currentUser.getId(), page, size);
        return Result.success(result);
    }

    /**
     * 获取我发起的审批列表
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping("/my")
    public Result<PageResult> getMyApprovals(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        PageResult result = approvalService.getMyApprovals(currentUser.getId(), page, size);
        return Result.success(result);
    }

    /**
     * 获取审批详情
     * @param recordId 审批记录ID
     * @return 审批详情
     */
    @GetMapping("/detail/{recordId}")
    public Result<ApprovalDetailVO> getApprovalDetail(@PathVariable Long recordId) {
        ApprovalDetailVO detail = approvalService.getApprovalDetail(recordId);
        if (detail == null) {
            return Result.error("审批记录不存在");
        }
        return Result.success(detail);
    }

    /**
     * 获取业务的审批状态
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 审批状态
     */
    @GetMapping("/status")
    public Result<ApprovalRecord> getApprovalStatus(
            @RequestParam String businessType,
            @RequestParam Long businessId) {
        ApprovalRecord record = approvalService.getApprovalStatus(businessType, businessId);
        return Result.success(record);
    }

    /**
     * 撤销审批
     * @param recordId 审批记录ID
     * @return 撤销结果
     */
    @PostMapping("/cancel/{recordId}")
    public Result<Void> cancelApproval(@PathVariable Long recordId) {
        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        try {
            approvalService.cancelApproval(recordId, currentUser.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("撤销审批失败", e);
            return Result.error(e.getMessage());
        }
    }
}
