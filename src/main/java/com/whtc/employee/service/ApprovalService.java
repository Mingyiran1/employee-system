package com.whtc.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.entity.ApprovalRecord;
import com.whtc.employee.vo.ApprovalDetailVO;

import java.util.List;

/**
 * 审批流程服务接口
 */
public interface ApprovalService extends IService<ApprovalRecord> {

    /**
     * 发起审批
     * @param businessType 业务类型（EMPLOYEE_ENTRY/EMPLOYEE_LEAVE）
     * @param businessId 业务ID
     * @param applicantId 申请人ID
     * @return 审批记录ID
     */
    Long startApproval(String businessType, Long businessId, Long applicantId);

    /**
     * 审批处理（通过/拒绝）
     * @param recordId 审批记录ID
     * @param approverId 审批人ID
     * @param approvalStatus 审批结果：1-通过 2-拒绝
     * @param comment 审批意见
     */
    void processApproval(Long recordId, Long approverId, Integer approvalStatus, String comment);

    /**
     * 查询我的待审批列表
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult getPendingApprovals(Long userId, Integer page, Integer size);

    /**
     * 查询我发起的审批列表
     * @param applicantId 申请人ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult getMyApprovals(Long applicantId, Integer page, Integer size);

    /**
     * 查询审批详情（包含审批历史）
     * @param recordId 审批记录ID
     * @return 审批详情
     */
    ApprovalDetailVO getApprovalDetail(Long recordId);

    /**
     * 查询业务的当前审批状态
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 审批记录，无则返回null
     */
    ApprovalRecord getApprovalStatus(String businessType, Long businessId);

    /**
     * 撤销审批
     * @param recordId 审批记录ID
     * @param applicantId 申请人ID
     */
    void cancelApproval(Long recordId, Long applicantId);
}
