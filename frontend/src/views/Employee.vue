<template>
  <div class="employee-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="保险公司">
          <el-select v-model="searchForm.companyId" placeholder="请选择保险公司" clearable>
            <el-option
              v-for="item in companyList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="劳务派遣公司">
          <el-select v-model="searchForm.supplierId" placeholder="请选择劳务派遣公司" clearable>
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位类型">
          <el-select v-model="searchForm.jobType" placeholder="请选择岗位类型" clearable>
            <el-option label="内勤" :value="1" />
            <el-option label="外勤" :value="2" />
            <el-option label="管理岗" :value="3" />
            <el-option label="技术岗" :value="4" />
            <el-option label="销售岗" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>保险员工列表</span>
          <div class="button-group">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增员工
            </el-button>
            <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>批量删除
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="idCard" label="身份证号" min-width="180" />
        <el-table-column prop="jobTypeName" label="岗位类型" min-width="100" />
        <el-table-column prop="companyName" label="所属保险公司" min-width="150" />
        <el-table-column prop="supplierName" label="劳务派遣公司" min-width="150" />
        <el-table-column prop="annualPremium" label="年保费(元)" min-width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.annualPremium) }}
          </template>
        </el-table-column>
        <el-table-column prop="dailyPremium" label="日保费(元)" min-width="120" align="right">
          <template #default="{ row }">
            <el-tag type="success">{{ formatMoney(row.dailyPremium) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hireDate" label="入职日期" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位类型" prop="jobType">
              <el-select v-model="form.jobType" placeholder="请选择岗位类型" style="width: 100%">
                <el-option label="内勤" :value="1" />
                <el-option label="外勤" :value="2" />
                <el-option label="管理岗" :value="3" />
                <el-option label="技术岗" :value="4" />
                <el-option label="销售岗" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属保险公司" prop="companyId">
              <el-select v-model="form.companyId" placeholder="请选择保险公司" style="width: 100%">
                <el-option
                  v-for="item in companyList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="劳务派遣公司" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择劳务派遣公司" style="width: 100%">
                <el-option
                  v-for="item in supplierList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保费计算方式" prop="premiumCalcType">
              <el-select v-model="form.premiumCalcType" placeholder="请选择计算方式" style="width: 100%">
                <el-option label="年费率计算" :value="1" />
                <el-option label="日费率直接设置" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年保费(元)" prop="annualPremium">
              <el-input-number
                v-model="form.annualPremium"
                :min="0"
                :precision="2"
                style="width: 100%"
                @change="calculateDailyPremium"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="日保费(元)">
              <el-input-number
                v-model="form.dailyPremium"
                :min="0"
                :precision="2"
                :disabled="form.premiumCalcType === 1"
                style="width: 100%"
              />
              <div v-if="form.premiumCalcType === 1" class="calc-tip">
                自动计算：年保费 × 费率 ÷ 365
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="form.hireDate"
                type="date"
                placeholder="请选择入职日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="离职日期">
              <el-date-picker
                v-model="form.leaveDate"
                type="date"
                placeholder="请选择离职日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">在职</el-radio>
                <el-radio :label="0">离职</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 简单的防抖函数
function debounce(fn, delay) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}
import {
  Search, Refresh, Plus, Delete, Edit, Download
} from '@element-plus/icons-vue'
import {
  getEmployeePage,
  getEmployeeById,
  addEmployee,
  updateEmployee,
  deleteEmployee,
  batchDeleteEmployee,
  calculateDailyPremium as calcDailyPremium
} from '@/api/insuranceEmployee'
import { getCompanyList } from '@/api/insuranceCompany'
import { getSupplierList } from '@/api/supplier'

// 搜索表单
const searchForm = reactive({
  name: '',
  companyId: null,
  supplierId: null,
  jobType: null,
  status: null
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedRows = ref([])

// 下拉列表数据
const companyList = ref([])
const supplierList = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  phone: '',
  idCard: '',
  jobType: 1,
  companyId: null,
  supplierId: null,
  annualPremium: 0,
  dailyPremium: 0,
  premiumCalcType: 1,
  hireDate: '',
  leaveDate: null,
  status: 1,
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  jobType: [{ required: true, message: '请选择岗位类型', trigger: 'change' }],
  companyId: [{ required: true, message: '请选择所属保险公司', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择劳务派遣公司', trigger: 'change' }],
  annualPremium: [{ required: true, message: '请输入年保费', trigger: 'blur' }],
  premiumCalcType: [{ required: true, message: '请选择保费计算方式', trigger: 'change' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 岗位类型映射
const jobTypeMap = {
  1: '内勤',
  2: '外勤',
  3: '管理岗',
  4: '技术岗',
  5: '销售岗'
}

// 格式化金额
const formatMoney = (value) => {
  if (value === null || value === undefined) return '-'
  return Number(value).toFixed(2)
}

// 加载表格数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const res = await getEmployeePage(params)
    if (res.code === 1) {
      tableData.value = res.data.records.map(item => ({
        ...item,
        jobTypeName: jobTypeMap[item.jobType] || '-'
      }))
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 加载下拉列表
const loadDropdownData = async () => {
  try {
    const [companyRes, supplierRes] = await Promise.all([
      getCompanyList(),
      getSupplierList()
    ])
    if (companyRes.code === 1) {
      companyList.value = companyRes.data
    }
    if (supplierRes.code === 1) {
      supplierList.value = supplierRes.data
    }
  } catch (error) {
    console.error('加载下拉数据失败:', error)
  }
}

// 计算日保费的核心逻辑
const _calculateDailyPremiumCore = async () => {
  if (form.premiumCalcType === 1 && form.annualPremium) {
    try {
      const res = await calcDailyPremium(form.annualPremium, form.premiumCalcType)
      if (res.code === 1) {
        form.dailyPremium = res.data
      }
    } catch (error) {
      // 使用前端计算作为备选（费率配置应该从后端获取）
      const rate = 0.015
      form.dailyPremium = (form.annualPremium * rate / 365).toFixed(2)
    }
  }
}

// 使用防抖包装计算日保费函数（防止频繁请求）
const calculateDailyPremium = debounce(_calculateDailyPremiumCore, 500)

// 监听保费计算方式变化
watch(() => form.premiumCalcType, () => {
  calculateDailyPremium()
})

// 搜索
const handleSearch = () => {
  page.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'name' ? '' : null
  })
  page.value = 1
  loadData()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增员工'
  Object.keys(form).forEach(key => {
    if (key === 'jobType' || key === 'premiumCalcType') {
      form[key] = 1
    } else if (key === 'status') {
      form[key] = 1
    } else if (key === 'annualPremium' || key === 'dailyPremium') {
      form[key] = 0
    } else {
      form[key] = null
    }
  })
  form.name = ''
  form.phone = ''
  form.idCard = ''
  form.hireDate = ''
  form.remark = ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑员工'
  try {
    const res = await getEmployeeById(row.id)
    if (res.code === 1) {
      Object.assign(form, res.data)
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取员工信息失败')
    }
  } catch (error) {
    ElMessage.error('获取员工信息失败')
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteEmployee(row.id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (!selectedRows.value.length) return
  const ids = selectedRows.value.map(row => row.id)
  ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 名员工吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await batchDeleteEmployee(ids)
      if (res.code === 1) {
        ElMessage.success('批量删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '批量删除失败')
      }
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const api = isEdit.value ? updateEmployee : addEmployee
    const res = await api(form)
    if (res.code === 1) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  }
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handleSizeChange = (val) => {
  size.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  page.value = val
  loadData()
}

onMounted(() => {
  loadData()
  loadDropdownData()
})
</script>

<style scoped>
.employee-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button-group {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.calc-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

:deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
</style>
