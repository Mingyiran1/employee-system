<template>
  <div class="employee-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptId" placeholder="请选择部门" clearable style="width: 180px">
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <div class="toolbar">
        <div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增员工</el-button>
          <el-button type="danger" :icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">
            批量删除
          </el-button>
        </div>
        <el-button type="success" :icon="Download" @click="handleExportClick">
          导出报表
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'primary' : 'danger'">
              {{ row.gender === 1 ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="entryDate" label="入职日期" width="110" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-button
              type="success"
              link
              :icon="CircleCheck"
              @click="handleApplyApproval(row)"
            >
              申请审批
            </el-button>
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

    <!-- 导出弹窗 -->
    <ExportDialog
      v-model="exportDialogVisible"
      :current-page-count="tableData.length"
      :total-count="pageInfo.total"
      :page="pageInfo.page"
      :size="pageInfo.size"
      :search-params="searchForm"
      @success="fetchData"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="入职日期" prop="entryDate">
          <el-date-picker
            v-model="form.entryDate"
            type="date"
            placeholder="请选择入职日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">离职</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="form.address" type="textarea" rows="2" placeholder="请输入家庭住址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, CircleCheck, Download } from '@element-plus/icons-vue'
import { getEmployeePage, addEmployee, updateEmployee, deleteEmployee, deleteEmployeeBatch, getEmployeeListAll } from '@/api/employee'
import ExportDialog from './components/ExportDialog.vue'
import { startApproval, getApprovalStatus } from '@/api/approval'
import { getDepartmentList } from '@/api/department'
import { PHONE, PHONE_MESSAGE, EMAIL, EMAIL_MESSAGE, ID_CARD, ID_CARD_MESSAGE } from '@/utils/regex'

// 搜索表单
const searchForm = reactive({
  name: '',
  deptId: null,
  status: null
})

// 分页信息
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])
const departmentList = ref([])

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const exportDialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  gender: 1,
  phone: '',
  email: '',
  idCard: '',
  deptId: null,
  position: '',
  entryDate: '',
  status: 1,
  address: ''
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: PHONE, message: PHONE_MESSAGE, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: EMAIL, message: EMAIL_MESSAGE, trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: ID_CARD, message: ID_CARD_MESSAGE, trigger: 'blur' }
  ],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }]
}

// 获取部门列表
const fetchDepartmentList = async () => {
  const res = await getDepartmentList()
  departmentList.value = res.data
}

// 获取员工列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getEmployeePage({
      page: pageInfo.page,
      size: pageInfo.size,
      ...searchForm
    })
    tableData.value = res.data.records
    pageInfo.total = res.data.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageInfo.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.deptId = null
  searchForm.status = null
  handleSearch()
}

// 多选
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增员工'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑员工'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除员工【${row.name}】吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteEmployee(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 申请入职审批
const handleApplyApproval = async (row) => {
  try {
    // 检查是否已有进行中的审批
    const checkRes = await getApprovalStatus('EMPLOYEE_ENTRY', row.id)
    if (checkRes.data && checkRes.data.approvalStatus === 0) {
      ElMessage.warning('该员工已有进行中的审批，请勿重复提交')
      return
    }

    await ElMessageBox.confirm(
      `确定要为【${row.name}】发起入职审批申请吗？`,
      '确认发起审批',
      {
        confirmButtonText: '确认发起',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await startApproval({
      businessType: 'EMPLOYEE_ENTRY',
      businessId: row.id
    })

    ElMessage.success('入职申请已提交，等待审批')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发起审批失败', error)
      ElMessage.error(error.response?.data?.message || '发起审批失败')
    }
  }
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 名员工吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteEmployeeBatch(selectedIds.value)
    ElMessage.success('批量删除成功')
    fetchData()
  })
}

// 点击导出按钮
const handleExportClick = () => {
  if (pageInfo.total === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  exportDialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateEmployee(form)
      ElMessage.success('更新成功')
    } else {
      await addEmployee(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.gender = 1
  form.phone = ''
  form.email = ''
  form.idCard = ''
  form.deptId = null
  form.position = ''
  form.entryDate = ''
  form.status = 1
  form.address = ''
  formRef.value?.resetFields()
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

onMounted(() => {
  fetchDepartmentList()
  fetchData()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
