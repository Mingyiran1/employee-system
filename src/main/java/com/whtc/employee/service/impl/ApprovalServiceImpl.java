package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.entity.*;
import com.whtc.employee.mapper.*;
import com.whtc.employee.service.ApprovalService;
import com.whtc.employee.service.MessageService;
import com.whtc.employee.vo.ApprovalDetailVO;
import com.whtc.employee.vo.ApprovalHistoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批流程服务实现
 * V2：支持按部门路由 + 消息通知
 */
@Service
@Slf4j
public class ApprovalServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> implements ApprovalService {

    @Autowired
    private ApprovalProcessMapper approvalProcessMapper;

    @Autowired
    private ApprovalNodeMapper approvalNodeMapper;

    @Autowired
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private MessageService messageService;

    private static final String BUSINESS_TYPE_ENTRY = "EMPLOYEE_ENTRY";
    private static final String BUSINESS_TYPE_LEAVE = "EMPLOYEE_LEAVE";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startApproval(String businessType, Long businessId, Long applicantId) {
        log.info("发起审批：businessType={}, businessId={}, applicantId={}", businessType, businessId, applicantId);

        // 0. 检查是否已有进行中的审批
        ApprovalRecord existingRecord = getPendingApprovalByBusiness(businessType, businessId);
        if (existingRecord != null) {
            throw new RuntimeException("该业务已有进行中的审批，请勿重复提交");
        }

        // 1. 查询员工信息，确定所属部门
        Employee employee = employeeMapper.selectById(businessId);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        // 入职审批：检查是否已有进行中的审批（允许已入职员工再次发起审批，用于演示）
        // 实际业务中可根据需求决定是否允许重复申请

        // 2. 查询审批流程定义
        LambdaQueryWrapper<ApprovalProcess> processWrapper = new LambdaQueryWrapper<>();
        processWrapper.eq(ApprovalProcess::getProcessType, businessType)
                .eq(ApprovalProcess::getStatus, 1)
                .orderByDesc(ApprovalProcess::getCreateTime);
        ApprovalProcess process = approvalProcessMapper.selectOne(processWrapper);

        if (process == null) {
            throw new RuntimeException("未找到对应的审批流程：" + businessType);
        }

        // 3. 根据员工部门获取第一个审批节点
        Long deptId = employee.getDeptId();
        ApprovalNode firstNode = getFirstNodeByDept(process.getId(), deptId);

        if (firstNode == null) {
            // 如果没有部门专属节点，使用默认节点
            firstNode = getDefaultFirstNode(process.getId());
        }

        if (firstNode == null) {
            throw new RuntimeException("审批流程未配置审批节点");
        }

        // 4. 创建审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessId(process.getId());
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setCurrentNodeId(firstNode.getId());
        record.setCurrentRoleId(firstNode.getRoleId());
        record.setApplicantId(applicantId);
        record.setApprovalStatus(0); // 待审批

        this.save(record);
        log.info("审批记录创建成功：recordId={}", record.getId());

        // 5. 发送消息通知给审批人
        sendApprovalNotification(record, firstNode, employee, true);

        return record.getId();
    }

    /**
     * 根据部门获取第一个审批节点（支持向上查找父部门）
     */
    private ApprovalNode getFirstNodeByDept(Long processId, Long deptId) {
        // 先查找当前部门
        ApprovalNode node = getNodeByDeptId(processId, deptId);
        if (node != null) {
            return node;
        }

        // 向上查找父部门
        Department dept = departmentMapper.selectById(deptId);
        while (dept != null && dept.getParentId() != null) {
            node = getNodeByDeptId(processId, dept.getParentId());
            if (node != null) {
                return node;
            }
            dept = departmentMapper.selectById(dept.getParentId());
        }

        return null;
    }

    /**
     * 根据具体部门ID获取审批节点
     */
    private ApprovalNode getNodeByDeptId(Long processId, Long deptId) {
        LambdaQueryWrapper<ApprovalNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalNode::getProcessId, processId)
                .eq(ApprovalNode::getStatus, 1)
                .eq(ApprovalNode::getDeptId, deptId)
                .orderByAsc(ApprovalNode::getNodeOrder)
                .last("LIMIT 1");
        return approvalNodeMapper.selectOne(wrapper);
    }

    /**
     * 获取默认第一个审批节点（无部门专属时使用）
     */
    private ApprovalNode getDefaultFirstNode(Long processId) {
        LambdaQueryWrapper<ApprovalNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalNode::getProcessId, processId)
                .eq(ApprovalNode::getStatus, 1)
                .isNull(ApprovalNode::getDeptId) // 通用节点
                .orderByAsc(ApprovalNode::getNodeOrder)
                .last("LIMIT 1");
        return approvalNodeMapper.selectOne(wrapper);
    }

    /**
     * 查询业务的进行中的审批
     */
    private ApprovalRecord getPendingApprovalByBusiness(String businessType, Long businessId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getBusinessType, businessType)
                .eq(ApprovalRecord::getBusinessId, businessId)
                .eq(ApprovalRecord::getApprovalStatus, 0) // 待审批
                .eq(ApprovalRecord::getIsDeleted, 0)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processApproval(Long recordId, Long approverId, Integer approvalStatus, String comment) {
        log.info("审批处理：recordId={}, approverId={}, approvalStatus={}", recordId, approverId, approvalStatus);

        // 1. 查询审批记录
        ApprovalRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }

        if (record.getApprovalStatus() != 0) {
            throw new RuntimeException("该审批已处理，请勿重复操作");
        }

        // 2. 验证审批人权限
        SysUser approver = sysUserMapper.selectById(approverId);
        if (approver == null) {
            throw new RuntimeException("审批人不存在");
        }

        // 3. 查询当前节点
        ApprovalNode currentNode = approvalNodeMapper.selectById(record.getCurrentNodeId());
        if (currentNode == null) {
            throw new RuntimeException("审批节点不存在");
        }

        // 4. 创建审批历史
        ApprovalHistory history = new ApprovalHistory();
        history.setRecordId(recordId);
        history.setNodeId(currentNode.getId());
        history.setApproverId(approverId);
        history.setApprovalStatus(approvalStatus);
        history.setApprovalComment(comment);
        history.setApprovalTime(LocalDateTime.now());
        approvalHistoryMapper.insert(history);

        // 5. 查询员工信息
        Employee employee = employeeMapper.selectById(record.getBusinessId());
        String employeeName = employee != null ? employee.getName() : "未知";

        // 6. 更新审批记录状态
        record.setApprovalStatus(approvalStatus);
        record.setApproverId(approverId);

        if (approvalStatus == 1) {
            // 通过：查找下一个节点
            Long deptId = employee != null ? employee.getDeptId() : null;
            ApprovalNode nextNode = getNextNode(record.getProcessId(), currentNode.getNodeOrder(), deptId);

            if (nextNode != null) {
                // 进入下一节点
                record.setCurrentNodeId(nextNode.getId());
                record.setCurrentRoleId(nextNode.getRoleId());
                record.setApprovalStatus(0); // 重置为待审批
                record.setApproverId(null); // 清除审批人
                log.info("进入下一审批节点：nodeId={}", nextNode.getId());

                // 发送消息通知给下一审批人
                sendApprovalNotification(record, nextNode, employee, true);
            } else {
                // 没有下一节点，审批完成
                log.info("审批流程完成：recordId={}", recordId);
                updateBusinessStatus(record);

                // 发送消息通知给申请人
                sendApprovalNotification(record, null, employee, false);
            }
        } else {
            // 拒绝：流程结束
            log.info("审批已拒绝：recordId={}", recordId);
            // 发送消息通知给申请人
            sendApprovalNotification(record, null, employee, false);
        }

        this.updateById(record);
    }

    /**
     * 获取下一个审批节点
     */
    private ApprovalNode getNextNode(Long processId, Integer currentOrder, Long deptId) {
        LambdaQueryWrapper<ApprovalNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalNode::getProcessId, processId)
                .eq(ApprovalNode::getStatus, 1)
                .gt(ApprovalNode::getNodeOrder, currentOrder)
                .and(w -> w.eq(ApprovalNode::getDeptId, deptId).or().isNull(ApprovalNode::getDeptId))
                .orderByAsc(ApprovalNode::getNodeOrder)
                .last("LIMIT 1");
        return approvalNodeMapper.selectOne(wrapper);
    }

    /**
     * 发送审批通知消息
     * @param toApprover true-发送给审批人，false-发送给申请人
     */
    private void sendApprovalNotification(ApprovalRecord record, ApprovalNode node, Employee employee, boolean toApprover) {
        try {
            String businessTypeName = getBusinessTypeName(record.getBusinessType());
            String employeeName = employee != null ? employee.getName() : "未知";

            if (toApprover && node != null) {
                // 查找该角色的所有用户（简化：只发消息给有管辖权的经理）
                Long roleId = node.getRoleId();
                Long deptId = node.getDeptId();

                LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(SysUser::getRoleId, roleId);

                // 如果是部门经理节点且有部门限定，只通知该部门经理
                if (deptId != null && "dept_manager".equals(node.getRoleCode())) {
                    userWrapper.eq(SysUser::getManagedDeptId, deptId);
                }

                List<SysUser> approvers = sysUserMapper.selectList(userWrapper);

                for (SysUser approver : approvers) {
                    String title = String.format("【待审批】%s - %s", businessTypeName, employeeName);
                    String content = String.format("您有一条%s待处理：%s（%s），请及时审批。",
                            node.getNodeName(), employeeName, businessTypeName);

                    messageService.sendApprovalMessage(
                            approver.getId(),
                            title,
                            content,
                            record.getBusinessType(),
                            record.getId()
                    );
                }
            } else {
                // 通知申请人审批结果
                String title, content;
                if (record.getApprovalStatus() == 1) {
                    title = String.format("【审批通过】%s - %s", businessTypeName, employeeName);
                    content = String.format("您的%s申请已通过：%s", businessTypeName, employeeName);
                } else if (record.getApprovalStatus() == 2) {
                    title = String.format("【审批拒绝】%s - %s", businessTypeName, employeeName);
                    content = String.format("您的%s申请已被拒绝：%s", businessTypeName, employeeName);
                } else {
                    return; // 状态未确定，不发送
                }

                messageService.sendApprovalMessage(
                        record.getApplicantId(),
                        title,
                        content,
                        record.getBusinessType(),
                        record.getId()
                );
            }
        } catch (Exception e) {
            log.error("发送审批通知失败", e);
            // 不影响主流程
        }
    }

    /**
     * 更新业务状态
     */
    private void updateBusinessStatus(ApprovalRecord record) {
        if (BUSINESS_TYPE_ENTRY.equals(record.getBusinessType())) {
            Employee employee = employeeMapper.selectById(record.getBusinessId());
            if (employee != null) {
                employee.setStatus(1); // 在职
                employeeMapper.updateById(employee);
                log.info("员工入职审批通过，更新状态：employeeId={}", employee.getId());
            }
        } else if (BUSINESS_TYPE_LEAVE.equals(record.getBusinessType())) {
            Employee employee = employeeMapper.selectById(record.getBusinessId());
            if (employee != null) {
                employee.setStatus(0); // 离职
                employeeMapper.updateById(employee);
                log.info("员工离职审批通过，更新状态：employeeId={}", employee.getId());
            }
        }
    }

    @Override
    public PageResult getPendingApprovals(Long userId, Integer page, Integer size) {
        Page<ApprovalRecord> pageInfo = new Page<>(page, size);

        // 获取当前用户信息
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            return new PageResult(0L, java.util.Collections.emptyList());
        }

        Long roleId = currentUser.getRoleId();

        // 普通员工(role_id=4)不应该看到待审批列表
        if (roleId != null && roleId == 4) {
            return new PageResult(0L, java.util.Collections.emptyList());
        }

        // 查询所有待审批记录
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getApprovalStatus, 0)
                .eq(ApprovalRecord::getIsDeleted, 0)
                .orderByDesc(ApprovalRecord::getCreateTime);

        Page<ApprovalRecord> result = this.page(pageInfo, wrapper);

        // 过滤：只显示当前用户有权限审批的
        List<ApprovalRecord> filteredRecords = result.getRecords().stream()
                .filter(record -> canUserApprove(currentUser, record))
                .collect(Collectors.toList());

        List<ApprovalDetailVO> voList = filteredRecords.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        return new PageResult((long) filteredRecords.size(), voList);
    }

    /**
     * 判断用户角色是否有权限审批指定角色节点
     * 支持角色层级：管理员 > 技术总监 > 部门经理 = HR
     */
    private boolean canRoleApprove(Long userRoleId, Long nodeRoleId) {
        if (userRoleId == null || nodeRoleId == null) {
            return false;
        }
        // 完全匹配
        if (userRoleId.equals(nodeRoleId)) {
            return true;
        }
        // 管理员可以审批所有
        if (userRoleId == 1) {
            return true;
        }
        // 技术总监(roleId=2)可以审批部门经理(roleId=3)和HR(roleId=4)的节点
        if (userRoleId == 2 && (nodeRoleId == 3 || nodeRoleId == 4)) {
            return true;
        }
        return false;
    }

    /**
     * 判断用户是否有权限审批某条记录
     */
    private boolean canUserApprove(SysUser user, ApprovalRecord record) {
        Long roleId = user.getRoleId();
        log.info("权限检查: userId={}, roleId={}, recordId={}, currentNodeId={}",
                user.getId(), roleId, record.getId(), record.getCurrentNodeId());

        // 管理员可以审批所有
        if (roleId != null && roleId == 1) {
            log.info("用户是管理员，有权限审批");
            return true;
        }

        // 获取当前审批节点
        ApprovalNode node = approvalNodeMapper.selectById(record.getCurrentNodeId());
        if (node == null) {
            log.warn("审批节点不存在: nodeId={}", record.getCurrentNodeId());
            return false;
        }
        log.info("审批节点: nodeId={}, nodeName={}, roleId={}, deptId={}",
                node.getId(), node.getNodeName(), node.getRoleId(), node.getDeptId());

        // 检查角色是否匹配（支持层级：高级角色可以审批低级角色的节点）
        // role_id: 1=管理员, 2=dept_CEO/技术总监, 3=dept_manager/部门经理, 4=普通员工
        Long nodeRoleId = node.getRoleId();
        if (!canRoleApprove(roleId, nodeRoleId)) {
            log.info("角色不匹配: node.roleId={}, user.roleId={}", nodeRoleId, roleId);
            return false;
        }

        // 如果节点有部门限定，检查用户是否管辖该部门
        if (node.getDeptId() != null) {
            // 获取员工的部门
            Employee employee = employeeMapper.selectById(record.getBusinessId());
            if (employee == null) {
                log.warn("员工不存在: employeeId={}", record.getBusinessId());
                return false;
            }
            log.info("员工部门: employeeId={}, deptId={}", employee.getId(), employee.getDeptId());

            // 检查员工部门是否匹配节点部门（支持层级）
            boolean match = isDeptMatchOrChild(node.getDeptId(), employee.getDeptId());
            log.info("部门匹配检查: node.deptId={}, employee.deptId={}, match={}",
                    node.getDeptId(), employee.getDeptId(), match);
            return match;
        }

        log.info("用户有权限审批");
        return true;
    }

    /**
     * 检查targetDeptId是否等于或属于parentDeptId的子部门
     */
    private boolean isDeptMatchOrChild(Long parentDeptId, Long targetDeptId) {
        if (parentDeptId == null || targetDeptId == null) {
            return false;
        }
        if (parentDeptId.equals(targetDeptId)) {
            return true;
        }

        // 向上查找targetDeptId的父部门
        Department dept = departmentMapper.selectById(targetDeptId);
        while (dept != null && dept.getParentId() != null) {
            if (parentDeptId.equals(dept.getParentId())) {
                return true;
            }
            dept = departmentMapper.selectById(dept.getParentId());
        }

        return false;
    }

    @Override
    public PageResult getMyApprovals(Long applicantId, Integer page, Integer size) {
        Page<ApprovalRecord> pageInfo = new Page<>(page, size);

        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getApplicantId, applicantId)
                .eq(ApprovalRecord::getIsDeleted, 0)
                .orderByDesc(ApprovalRecord::getCreateTime);

        Page<ApprovalRecord> result = this.page(pageInfo, wrapper);

        List<ApprovalDetailVO> voList = result.getRecords().stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        return new PageResult(result.getTotal(), voList);
    }

    @Override
    public ApprovalDetailVO getApprovalDetail(Long recordId) {
        ApprovalRecord record = this.getById(recordId);
        if (record == null) {
            return null;
        }

        ApprovalDetailVO vo = convertToDetailVO(record);

        LambdaQueryWrapper<ApprovalHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalHistory::getRecordId, recordId)
                .orderByAsc(ApprovalHistory::getCreateTime);
        List<ApprovalHistory> historyList = approvalHistoryMapper.selectList(wrapper);

        List<ApprovalHistoryVO> historyVOList = historyList.stream()
                .map(this::convertToHistoryVO)
                .collect(Collectors.toList());

        vo.setHistoryList(historyVOList);

        return vo;
    }

    @Override
    public ApprovalRecord getApprovalStatus(String businessType, Long businessId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getBusinessType, businessType)
                .eq(ApprovalRecord::getBusinessId, businessId)
                .orderByDesc(ApprovalRecord::getCreateTime)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApproval(Long recordId, Long applicantId) {
        ApprovalRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("审批记录不存在");
        }

        if (!record.getApplicantId().equals(applicantId)) {
            throw new RuntimeException("只能撤销自己发起的审批");
        }

        if (record.getApprovalStatus() != 0) {
            throw new RuntimeException("只能撤销进行中的审批");
        }

        record.setApprovalStatus(3); // 3-已撤销
        this.updateById(record);

        log.info("审批已撤销：recordId={}", recordId);

        // 发送撤销通知给当前审批人（如果有）
        if (record.getCurrentRoleId() != null) {
            LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(SysUser::getRoleId, record.getCurrentRoleId());
            List<SysUser> approvers = sysUserMapper.selectList(userWrapper);

            for (SysUser approver : approvers) {
                messageService.sendApprovalMessage(
                        approver.getId(),
                        "【审批已撤销】",
                        "一条审批申请已被申请人撤销",
                        record.getBusinessType(),
                        record.getId()
                );
            }
        }
    }

    private ApprovalDetailVO convertToDetailVO(ApprovalRecord record) {
        ApprovalDetailVO vo = new ApprovalDetailVO();
        vo.setId(record.getId());
        vo.setBusinessType(record.getBusinessType());
        vo.setBusinessTypeName(getBusinessTypeName(record.getBusinessType()));
        vo.setBusinessId(record.getBusinessId());
        vo.setApprovalStatus(record.getApprovalStatus());
        vo.setApprovalStatusName(getStatusName(record.getApprovalStatus()));
        vo.setApplicantId(record.getApplicantId());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());

        SysUser applicant = sysUserMapper.selectById(record.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getRealName());
        }

        if (record.getCurrentNodeId() != null) {
            ApprovalNode node = approvalNodeMapper.selectById(record.getCurrentNodeId());
            if (node != null) {
                vo.setCurrentNodeName(node.getNodeName());
            }
        }

        // 查询员工姓名
        Employee employee = employeeMapper.selectById(record.getBusinessId());
        if (employee != null) {
            vo.setEmployeeName(employee.getName());
        }

        return vo;
    }

    private ApprovalHistoryVO convertToHistoryVO(ApprovalHistory history) {
        ApprovalHistoryVO vo = new ApprovalHistoryVO();
        vo.setId(history.getId());
        vo.setApproverId(history.getApproverId());
        vo.setApprovalStatus(history.getApprovalStatus());
        vo.setApprovalStatusName(history.getApprovalStatus() == 1 ? "通过" : "拒绝");
        vo.setApprovalComment(history.getApprovalComment());
        vo.setApprovalTime(history.getApprovalTime());
        vo.setCreateTime(history.getCreateTime());

        SysUser approver = sysUserMapper.selectById(history.getApproverId());
        if (approver != null) {
            vo.setApproverName(approver.getRealName());
        }

        ApprovalNode node = approvalNodeMapper.selectById(history.getNodeId());
        if (node != null) {
            vo.setNodeName(node.getNodeName());
        }

        return vo;
    }

    private String getBusinessTypeName(String type) {
        return switch (type) {
            case BUSINESS_TYPE_ENTRY -> "员工入职";
            case BUSINESS_TYPE_LEAVE -> "员工离职";
            default -> "未知";
        };
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待审批";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已撤销";
            default -> "未知";
        };
    }
}
