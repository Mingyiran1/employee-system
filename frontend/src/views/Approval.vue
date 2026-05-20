<template>
  <div class="approval-container">
    <!-- Tab切换 -->
    <el-card class="tab-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我发起的" name="my">
          <el-badge :value="myApprovalCount" :hidden="myApprovalCount === 0" />
        </el-tab-pane>
        <el-tab-pane v-if="canApprove" label="待我审批" name="pending">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="pending-badge" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" :icon="Plus" @click="handleStartApproval">
            发起入职审批
          </el-button>
        </div>
        <el-radio-group v-model="filterStatus" @change="handleFilterChange" size="small">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">待审批</el-radio-button>
          <el-radio-button label="1">已通过</el-radio-button>
          <el-radio-button label="2">已拒绝</el-radio-button>
          <el-radio-button label="3">已撤销</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="id" label="审批单号" width="120" />
        <el-table-column prop="businessTypeName" label="审批类型" width="120" />
        <el-table-column label="员工信息" min-width="200">
          <template #default="{ row }">
            <div class="employee-info">
              <el-avatar :size="32" :icon="UserFilled" class="employee-avatar" />
              <div class="employee-detail">
                <div class="employee-name">{{ row.employeeName || '-' }}</div>
                <div class="employee-id">ID: {{ row.businessId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" v-if="activeTab === 'pending'" />
        <el-table-column prop="currentNodeName" label="当前节点" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.currentNodeName" type="warning" size="small">
              {{ row.currentNodeName }}
            </el-tag>
            <el-tag v-else-if="row.approvalStatus === 0" type="info" size="small">待分配</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.approvalStatus)" size="small">
              {{ row.approvalStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleViewDetail(row)">详情</el-button>

            <!-- 撤销按钮：我发起的 + 待审批状态 -->
            <el-button
              v-if="activeTab === 'my' && row.approvalStatus === 0"
              type="warning"
              link
              :icon="CircleClose"
              @click="handleCancel(row)"
            >
              撤销
            </el-button>

            <!-- 催办按钮：我发起的 + 待审批状态 -->
            <el-button
              v-if="activeTab === 'my' && row.approvalStatus === 0"
              type="info"
              link
              :icon="Bell"
              @click="handleRemind(row)"
            >
              催办
            </el-button>

            <!-- 审批按钮：待我审批 + 待审批状态 -->
            <template v-if="activeTab === 'pending' && row.approvalStatus === 0">
              <el-button type="success" link :icon="Check" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" link :icon="Close" @click="handleReject(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageInfo.page"
        v-model:page-size="pageInfo.size"
        :total="pageInfo.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 发起审批弹窗 -->
    <el-dialog
      v-model="startDialogVisible"
      title="发起员工入职审批"
      width="550px"
      destroy-on-close
    >
      <el-alert
        title="提示：只能选择未入职的员工发起审批，且该员工不能已有进行中的审批"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      />
      <el-form :model="startForm" :rules="startFormRules" ref="startFormRef" label-width="100px">
        <!-- 普通员工：自动使用自己，隐藏选择 -->
        <el-form-item v-if="isRegularEmployee" label="申请员工">
          <span>{{ currentUser.realName || currentUser.username }}</span>
        </el-form-item>
        <!-- 经理/HR/管理员：显示员工选择 -->
        <el-form-item v-else label="选择员工" prop="employeeId">
          <el-select
            v-model="startForm.employeeId"
            placeholder="请选择员工"
            style="width: 100%"
            filterable
            :loading="employeeLoading"
          >
            <el-option
              v-for="emp in eligibleEmployees"
              :key="emp.id"
              :label="`${emp.name} - ${emp.deptName || '未分配部门'}`"
              :value="emp.id"
            >
              <div class="employee-option">
                <span class="emp-name">{{ emp.name }}</span>
                <span class="emp-dept">{{ emp.deptName || '未分配部门' }}</span>
                <span class="emp-status" :class="emp.status === 0 ? 'status-inactive' : 'status-active'">
                  {{ emp.status === 0 ? '待入职' : '在职' }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="startForm.reason"
            type="textarea"
            rows="3"
            placeholder="请输入申请理由（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitStart">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 审批处理弹窗 -->
    <el-dialog
      v-model="processDialogVisible"
      :title="processTitle"
      width="500px"
      destroy-on-close
    >
      <el-form :model="processForm" :rules="processFormRules" ref="processFormRef" label-width="100px">
        <el-form-item label="审批单号">
          <span>{{ currentRow?.id }}</span>
        </el-form-item>
        <el-form-item label="员工姓名">
          <span>{{ currentRow?.employeeName }}</span>
        </el-form-item>
        <el-form-item label="业务类型">
          <span>{{ currentRow?.businessTypeName }}</span>
        </el-form-item>
        <el-form-item label="当前节点">
          <span>{{ currentRow?.currentNodeName }}</span>
        </el-form-item>
        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="processForm.comment"
            type="textarea"
            rows="4"
            :placeholder="processForm.approvalStatus === 1 ? '请输入通过意见（选填）' : '请输入拒绝理由（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitProcess">确认</el-button>
      </template>
    </el-dialog>

    <!-- 审批详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="审批详情"
      width="750px"
      destroy-on-close
    >
      <div v-if="approvalDetail" class="approval-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="审批单号">{{ approvalDetail.id }}</el-descriptions-item>
            <el-descriptions-item label="审批类型">{{ approvalDetail.businessTypeName }}</el-descriptions-item>
            <el-descriptions-item label="员工姓名">{{ approvalDetail.employeeName }}</el-descriptions-item>
            <el-descriptions-item label="员工ID">{{ approvalDetail.businessId }}</el-descriptions-item>
            <el-descriptions-item label="审批状态">
              <el-tag :type="getStatusType(approvalDetail.approvalStatus)">
                {{ approvalDetail.approvalStatusName }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请人">{{ approvalDetail.applicantName }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ approvalDetail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ approvalDetail.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 审批流程时间线 -->
        <div class="detail-section">
          <h4>审批流程</h4>
          <el-timeline>
            <!-- 申请节点 -->
            <el-timeline-item
              :type="'primary'"
              :icon="Plus"
              :timestamp="approvalDetail.createTime"
            >
              <div class="timeline-content">
                <div class="timeline-title">申请人提交</div>
                <div class="timeline-user">{{ approvalDetail.applicantName }}</div>
              </div>
            </el-timeline-item>

            <!-- 审批历史节点 -->
            <el-timeline-item
              v-for="(history, index) in approvalDetail.historyList"
              :key="index"
              :type="history.approvalStatus === 1 ? 'success' : 'danger'"
              :icon="history.approvalStatus === 1 ? Check : Close"
              :timestamp="history.approvalTime"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ history.nodeName }}</div>
                <div class="timeline-user">
                  {{ history.approverName }}
                  <el-tag :type="history.approvalStatus === 1 ? 'success' : 'danger'" size="small" class="status-tag">
                    {{ history.approvalStatusName }}
                  </el-tag>
                </div>
                <div v-if="history.approvalComment" class="timeline-comment">
                  意见：{{ history.approvalComment }}
                </div>
              </div>
            </el-timeline-item>

            <!-- 当前待审批节点 -->
            <el-timeline-item
              v-if="approvalDetail.approvalStatus === 0"
              type="warning"
              :icon="Timer"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ approvalDetail.currentNodeName || '等待分配审批人' }}</div>
                <div class="timeline-user">
                  <el-tag type="warning" size="small">处理中</el-tag>
                </div>
              </div>
            </el-timeline-item>

            <!-- 流程结束 -->
            <el-timeline-item
              v-else
              :type="approvalDetail.approvalStatus === 1 ? 'success' : approvalDetail.approvalStatus === 2 ? 'danger' : 'info'"
              :icon="Finished"
            >
              <div class="timeline-content">
                <div class="timeline-title">
                  {{ approvalDetail.approvalStatus === 3 ? '已撤销' : '审批完成' }}
                </div>
                <div class="timeline-user">
                  <el-tag :type="getStatusType(approvalDetail.approvalStatus)" size="small">
                    {{ approvalDetail.approvalStatusName }}
                  </el-tag>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed, onBeforeMount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Check, Close, Timer, Finished, CircleClose, Bell, UserFilled } from '@element-plus/icons-vue'
import {
  getMyApprovals,
  getPendingApprovals,
  startApproval,
  processApproval,
  getApprovalDetail,
  cancelApproval
} from '@/api/approval'
import { getEmployeeListAll, getCurrentEmployee } from '@/api/employee'
import { getApprovalStatus } from '@/api/approval'

// Tab和过滤
const activeTab = ref('my')
const filterStatus = ref('')
const loading = ref(false)
const pendingCount = ref(0)
const myApprovalCount = ref(0)

// 当前用户信息
const currentUser = ref(JSON.parse(localStorage.getItem('user') || '{}'))

// 是否有审批权限（非普通员工）
const canApprove = computed(() => {
  // roleId: 1=管理员, 2=部门经理, 3=HR, 4=普通员工
  // 普通员工(roleId=4)不能审批
  return currentUser.value.roleId !== 4
})

// 是否为普通员工
const isRegularEmployee = computed(() => {
  return currentUser.value.roleId === 4
})

// 表格数据
const tableData = ref([])
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 发起审批弹窗
const startDialogVisible = ref(false)
const startFormRef = ref()
const submitLoading = ref(false)
const employeeLoading = ref(false)
const employeeList = ref([])
const eligibleEmployees = computed(() => {
  // 显示所有有部门的员工（用于演示，实际业务可能需要过滤已入职员工）
  return employeeList.value.filter(emp => emp.deptId != null)
})

const startForm = reactive({
  employeeId: null,
  reason: ''
})

const startFormRules = computed(() => {
  // 普通员工自动使用自己，不需要选择
  if (isRegularEmployee.value) {
    return {}
  }
  return {
    employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }]
  }
})

// 审批处理弹窗
const processDialogVisible = ref(false)
const processFormRef = ref()
const processTitle = ref('')
const currentRow = ref(null)

const processForm = reactive({
  approvalStatus: 1,
  comment: ''
})

const processFormRules = {
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

// 审批详情弹窗
const detailDialogVisible = ref(false)
const approvalDetail = ref(null)

// 状态类型映射
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    case 3: return 'info'
    default: return 'info'
  }
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pageInfo.page,
      size: pageInfo.size
    }

    let res
    if (activeTab.value === 'my') {
      res = await getMyApprovals(params)
    } else {
      res = await getPendingApprovals(params)
    }

    // 过滤状态
    let records = res.data.records || []
    if (filterStatus.value !== '') {
      records = records.filter(item => String(item.approvalStatus) === filterStatus.value)
    }

    tableData.value = records
    pageInfo.total = res.data.total || 0

    // 更新角标数量
    if (activeTab.value === 'my') {
      myApprovalCount.value = pageInfo.total
    } else {
      pendingCount.value = pageInfo.total
    }
  } finally {
    loading.value = false
  }
}

// Tab切换
const handleTabChange = () => {
  pageInfo.page = 1
  filterStatus.value = ''
  fetchData()
}

// 状态过滤
const handleFilterChange = () => {
  pageInfo.page = 1
  fetchData()
}

// 分页
const handleSizeChange = (val) => {
  pageInfo.size = val
  fetchData()
}

const handlePageChange = (val) => {
  pageInfo.page = val
  fetchData()
}

// 当前登录用户的员工信息（普通员工使用）
const currentEmployee = ref(null)

// 发起审批
const handleStartApproval = async () => {
  // 普通员工：自动获取自己的员工信息
  if (isRegularEmployee.value) {
    try {
      const res = await getCurrentEmployee()
      currentEmployee.value = res.data
      startForm.employeeId = res.data?.id || null
    } catch (error) {
      console.error('获取当前员工信息失败', error)
      ElMessage.error('获取员工信息失败')
      return
    }
  } else {
    // 经理/HR/管理员：加载员工列表
    employeeLoading.value = true
    try {
      const res = await getEmployeeListAll()
      employeeList.value = res.data || []
    } catch (error) {
      console.error('获取员工列表失败', error)
      ElMessage.error('获取员工列表失败')
    } finally {
      employeeLoading.value = false
    }
  }

  startForm.reason = ''
  startDialogVisible.value = true
}

const handleSubmitStart = async () => {
  // 普通员工不需要表单验证
  if (!isRegularEmployee.value) {
    const valid = await startFormRef.value.validate().catch(() => false)
    if (!valid) return
  }

  // 检查是否有employeeId
  if (!startForm.employeeId) {
    ElMessage.error('未获取到员工信息')
    return
  }

  // 检查是否已有进行中的审批
  const checkRes = await getApprovalStatus('EMPLOYEE_ENTRY', startForm.employeeId)
  if (checkRes.data && checkRes.data.approvalStatus === 0) {
    ElMessage.warning('该员工已有进行中的审批，请勿重复提交')
    return
  }

  submitLoading.value = true
  try {
    await startApproval({
      businessType: 'EMPLOYEE_ENTRY',
      businessId: startForm.employeeId
    })
    ElMessage.success('入职申请已提交，等待审批')
    startDialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

// 审批处理
const handleApprove = (row) => {
  currentRow.value = row
  processTitle.value = '审批通过'
  processForm.approvalStatus = 1
  processForm.comment = ''
  processDialogVisible.value = true
}

const handleReject = (row) => {
  currentRow.value = row
  processTitle.value = '审批拒绝'
  processForm.approvalStatus = 2
  processForm.comment = ''
  processDialogVisible.value = true
}

const handleSubmitProcess = async () => {
  if (processForm.approvalStatus === 2 && !processForm.comment.trim()) {
    ElMessage.warning('拒绝时必须填写审批意见')
    return
  }

  submitLoading.value = true
  try {
    await processApproval(currentRow.value.id, {
      approvalStatus: processForm.approvalStatus,
      comment: processForm.comment
    })
    ElMessage.success(processForm.approvalStatus === 1 ? '审批通过' : '已拒绝')
    processDialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

// 撤销审批
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤销这条审批申请吗？撤销后需要重新发起', '确认撤销', {
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelApproval(row.id)
    ElMessage.success('审批已撤销')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销失败', error)
    }
  }
}

// 催办
const handleRemind = (row) => {
  ElMessage.success(`已向审批人发送催办通知（审批单号：${row.id}）`)
}

// 查看详情
const handleViewDetail = async (row) => {
  const res = await getApprovalDetail(row.id)
  approvalDetail.value = res.data
  detailDialogVisible.value = true
}

// 初始化
onMounted(() => {
  // 如果普通员工且当前在pending tab，自动切换到my
  if (!canApprove.value && activeTab.value === 'pending') {
    activeTab.value = 'my'
  }
  fetchData()
})

// 监听Tab变化自动刷新
watch(activeTab, () => {
  fetchData()
})
</script>

<style scoped>
.tab-card {
  margin-bottom: 20px;
}

.tab-card :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.pending-badge :deep(.el-badge__content) {
  background-color: #f56c6c;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

/* 员工信息显示 */
.employee-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.employee-avatar {
  background-color: #409EFF;
}

.employee-detail {
  display: flex;
  flex-direction: column;
}

.employee-name {
  font-weight: 500;
  color: #303133;
}

.employee-id {
  font-size: 12px;
  color: #909399;
}

/* 员工选择下拉样式 */
.employee-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.employee-option .emp-name {
  font-weight: 500;
  min-width: 80px;
}

.employee-option .emp-dept {
  color: #909399;
  font-size: 12px;
  flex: 1;
}

.employee-option .emp-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.employee-option .status-inactive {
  background-color: #f4f4f5;
  color: #909399;
}

.employee-option .status-active {
  background-color: #f0f9eb;
  color: #67c23a;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

/* 审批详情样式 */
.approval-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
  color: #303133;
  font-size: 16px;
}

.timeline-content {
  padding: 8px 0;
}

.timeline-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.timeline-user {
  color: #606266;
  font-size: 14px;
}

.timeline-user .status-tag {
  margin-left: 8px;
}

.timeline-comment {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
}
</style>
