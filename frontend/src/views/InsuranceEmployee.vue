<template>
  <div class="insurance-employee-container">
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="searchForm.idCard" placeholder="请输入身份证号" clearable />
        </el-form-item>
        <el-form-item label="投保公司">
          <el-select v-model="searchForm.companyId" placeholder="请选择投保公司" clearable style="width: 180px">
            <el-option
              v-for="item in companyOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="请选择供应商" clearable style="width: 180px">
            <el-option
              v-for="item in supplierOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工种">
          <el-select v-model="searchForm.jobType" placeholder="请选择工种" clearable style="width: 180px">
            <el-option label="一类" value="一类" />
            <el-option label="二类" value="二类" />
            <el-option label="三类" value="三类" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="入职日期">
          <el-date-picker
            v-model="hireDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="离职日期">
          <el-date-picker
            v-model="leaveDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button v-if="!isRegularUser" type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        <el-button v-if="!isRegularUser" type="danger" :icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button v-if="!isRegularUser" type="success" :icon="Upload" @click="handleImport">导入Excel</el-button>
        <el-button v-if="!isRegularUser" :icon="Download" @click="handleDownloadTemplate">下载模板</el-button>
        <el-button type="warning" :icon="Download" @click="handleExport">导出Excel</el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column v-if="!isRegularUser" type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="jobType" label="工种" width="80">
          <template #default="{ row }">
            <el-tag :type="getJobTypeTag(row.jobType)">{{ row.jobType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="投保公司" min-width="120" />
        <el-table-column prop="supplierName" label="供应商" min-width="120" />
        <el-table-column prop="dailyPremium" label="保费标准(天)" min-width="120">
          <template #default="{ row }">
            <span v-if="row.dailyPremium">¥{{ row.dailyPremium }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="realTimePremium" label="实时保费" min-width="120">
          <template #default="{ row }">
            <span v-if="row.realTimePremium" style="color: #67C23A; font-weight: bold;">¥{{ row.realTimePremium }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="hireDate" label="入职时间" min-width="120">
          <template #default="{ row }">
            {{ row.hireDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="leaveDate" label="离职时间" min-width="120">
          <template #default="{ row }">
            <span v-if="row.leaveDate" style="color: #F56C6C;">{{ row.leaveDate }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isRegularUser" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="投保公司" prop="companyId">
          <el-select v-model="form.companyId" placeholder="请选择投保公司" style="width: 100%">
            <el-option
              v-for="item in companyOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option
              v-for="item in supplierOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工种" prop="jobType">
          <el-select v-model="form.jobType" placeholder="请选择工种" @change="handleJobTypeChange" style="width: 100%">
            <el-option label="一类" value="一类" />
            <el-option label="二类" value="二类" />
            <el-option label="三类" value="三类" />
          </el-select>
        </el-form-item>
        <el-form-item label="年保费">
          <el-input v-model="form.annualPremium" disabled placeholder="选择工种后自动计算">
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="入职日期" prop="hireDate">
          <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择入职日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" @change="handleStatusChange" style="width: 100%">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="离职日期" prop="leaveDate" v-if="form.status === 2">
          <el-date-picker v-model="form.leaveDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择离职日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="importDialogVisible"
      title="导入保险员工"
      width="500px"
    >
      <el-upload
        ref="uploadRef"
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        accept=".xlsx,.xls"
        drag
        style="width: 100%"
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .xlsx, .xls 格式，请先下载模板填写数据
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImportSubmit">确定导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit, Upload, Download } from '@element-plus/icons-vue'
import {
  getInsuranceEmployeePage,
  getInsuranceEmployeeById,
  addInsuranceEmployee,
  updateInsuranceEmployee,
  deleteInsuranceEmployee,
  deleteInsuranceEmployeeBatch,
  importInsuranceEmployee,
  downloadTemplate,
  exportInsuranceEmployee
} from '@/api/insuranceEmployee'
import { getSupplierList } from '@/api/supplier'
import { getPremiumConfigList } from '@/api/premiumConfig'
import { getCompanyList } from '@/api/insuranceCompany'

const userInfo = ref(JSON.parse(sessionStorage.getItem('user') || '{}'))
const isRegularUser = computed(() => userInfo.value.roleId === 4)

const searchForm = reactive({
  name: '',
  idCard: '',
  companyId: null,
  supplierId: null,
  jobType: null,
  status: null,
  hireDateStart: null,
  hireDateEnd: null,
  leaveDateStart: null,
  leaveDateEnd: null
})
const hireDateRange = ref([])
const leaveDateRange = ref([])

const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])
const supplierOptions = ref([])
const companyOptions = ref([])
const premiumConfigList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增保险员工')
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  idCard: '',
  phone: '',
  email: '',
  companyId: null,
  supplierId: null,
  jobType: null,
  annualPremium: null,
  hireDate: null,
  leaveDate: null,
  status: 1,
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  companyId: [{ required: true, message: '请选择投保公司', trigger: 'change' }],
  jobType: [{ required: true, message: '请选择工种', trigger: 'change' }]
}

const importDialogVisible = ref(false)
const uploadRef = ref()
const importLoading = ref(false)
const importFile = ref(null)

const syncDateRanges = () => {
  searchForm.hireDateStart = hireDateRange.value?.[0] || null
  searchForm.hireDateEnd = hireDateRange.value?.[1] || null
  searchForm.leaveDateStart = leaveDateRange.value?.[0] || null
  searchForm.leaveDateEnd = leaveDateRange.value?.[1] || null
}

const fetchData = async () => {
  loading.value = true
  try {
    syncDateRanges()
    const res = await getInsuranceEmployeePage({
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

const fetchSupplierOptions = async () => {
  try {
    const res = await getSupplierList()
    supplierOptions.value = res.data || []
  } catch (error) {
    console.error('获取供应商列表失败', error)
  }
}

const fetchCompanyOptions = async () => {
  try {
    const res = await getCompanyList()
    companyOptions.value = res.data || []
  } catch (error) {
    console.error('获取投保公司列表失败', error)
  }
}

const fetchPremiumConfig = async () => {
  try {
    const res = await getPremiumConfigList()
    premiumConfigList.value = res.data || []
  } catch (error) {
    console.error('获取保费配置失败', error)
  }
}

const handleJobTypeChange = (value) => {
  const config = premiumConfigList.value.find(item => item.jobType === value)
  if (config && config.annualPremium) {
    form.annualPremium = config.annualPremium
  } else {
    form.annualPremium = null
  }
}

const handleStatusChange = (value) => {
  if (value === 1) {
    // 状态改为在职，清空离职日期
    form.leaveDate = null
  }
}

const getJobTypeTag = (jobType) => {
  const map = { '一类': 'success', '二类': 'warning', '三类': 'danger' }
  return map[jobType] || ''
}

const handleSearch = () => {
  pageInfo.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.idCard = ''
  searchForm.companyId = null
  searchForm.supplierId = null
  searchForm.jobType = null
  searchForm.status = null
  searchForm.hireDateStart = null
  searchForm.hireDateEnd = null
  searchForm.leaveDateStart = null
  searchForm.leaveDateEnd = null
  hireDateRange.value = []
  leaveDateRange.value = []
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAdd = () => {
  if (isRegularUser.value) {
    ElMessage.warning('当前账号仅可查看保险员工信息')
    return
  }
  isEdit.value = false
  dialogTitle.value = '新增保险员工'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  if (isRegularUser.value) {
    ElMessage.warning('当前账号仅可查看保险员工信息')
    return
  }
  isEdit.value = true
  dialogTitle.value = '编辑保险员工'
  try {
    const res = await getInsuranceEmployeeById(row.id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取员工信息失败')
  }
}

const handleDelete = (row) => {
  if (isRegularUser.value) {
    ElMessage.warning('当前账号仅可查看保险员工信息')
    return
  }
  ElMessageBox.confirm(`确定要删除员工【${row.name}】吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteInsuranceEmployee(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleBatchDelete = () => {
  if (isRegularUser.value) {
    ElMessage.warning('当前账号仅可查看保险员工信息')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 名员工吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteInsuranceEmployeeBatch(selectedIds.value)
    ElMessage.success('批量删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  if (isRegularUser.value) {
    ElMessage.warning('当前账号仅可查看保险员工信息')
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateInsuranceEmployee(form)
      ElMessage.success('更新成功')
    } else {
      await addInsuranceEmployee(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleImport = () => {
  if (isRegularUser.value) {
    ElMessage.warning('当前账号仅可查看保险员工信息')
    return
  }
  importFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel' ||
                  file.name.endsWith('.xlsx') ||
                  file.name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('请上传 Excel 文件!')
  }
  return isExcel
}

const handleImportSubmit = async () => {
  if (isRegularUser.value) {
    ElMessage.warning('当前账号仅可查看保险员工信息')
    return
  }
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  importLoading.value = true
  try {
    const res = await importInsuranceEmployee(importFile.value)
    ElMessage.success(`导入成功 ${res.data.successCount} 条数据`)
    if (res.data.errorCount > 0) {
      ElMessage.warning(`${res.data.errorCount} 条数据导入失败`)
      console.log('导入错误:', res.data.errorMessages)
    }
    importDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

const handleDownloadTemplate = () => {
  downloadTemplate()
}

const handleExport = async () => {
  try {
    syncDateRanges()
    await exportInsuranceEmployee({ ...searchForm })
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.idCard = ''
  form.phone = ''
  form.email = ''
  form.companyId = null
  form.supplierId = null
  form.jobType = null
  form.annualPremium = null
  form.hireDate = null
  form.leaveDate = null
  form.status = 1
  form.remark = ''
  formRef.value?.resetFields()
}

const handleSizeChange = (val) => {
  pageInfo.size = val
  fetchData()
}

const handlePageChange = (val) => {
  pageInfo.page = val
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchSupplierOptions()
  fetchCompanyOptions()
  fetchPremiumConfig()
})
</script>

<style scoped>
.insurance-employee-container {
  padding: 20px;
}

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
