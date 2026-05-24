<template>
  <div class="premium-config-container">
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="工种">
          <el-select v-model="searchForm.jobType" placeholder="请选择工种" clearable style="width: 180px">
            <el-option label="一类" value="一类" />
            <el-option label="二类" value="二类" />
            <el-option label="三类" value="三类" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>保费配置管理</span>
          <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="handleAdd">新增配置</el-button>
        </div>
      </template>

      <el-table :data="filteredTableData" v-loading="loading" border>
        <el-table-column prop="jobType" label="工种" width="150">
          <template #default="{ row }">
            <el-tag :type="getJobTypeTag(row.jobType)">{{ row.jobType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rate" label="费率" width="150">
          <template #default="{ row }">
            {{ (row.rate * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column prop="baseSalary" label="基数（年薪）" width="180">
          <template #default="{ row }">
            ¥{{ row.baseSalary?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="annualPremium" label="年保费" width="180">
          <template #default="{ row }">
            <span class="premium-amount">¥{{ row.annualPremium?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="isAdmin" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="isAdmin" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
            <span v-if="!isAdmin" style="color: #909399; font-size: 12px;">仅查看</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="formula-note">
        <el-alert type="info" :closable="false">
          <template #title>
            计算公式：<strong>年保费 = 基数 × 费率</strong>
          </template>
        </el-alert>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="工种" prop="jobType">
          <el-select v-model="form.jobType" placeholder="请选择工种" style="width: 100%" @change="calculatePremium">
            <el-option label="一类" value="一类" />
            <el-option label="二类" value="二类" />
            <el-option label="三类" value="三类" />
          </el-select>
        </el-form-item>
        <el-form-item label="费率" prop="rate">
          <el-input-number
            v-model="form.rate"
            :precision="4"
            :min="0"
            :max="1"
            :step="0.001"
            style="width: 100%"
            @change="calculatePremium"
          />
          <div class="rate-hint">当前费率：{{ (form.rate * 100).toFixed(2) }}%</div>
        </el-form-item>
        <el-form-item label="基数（年薪）" prop="baseSalary">
          <el-input-number
            v-model="form.baseSalary"
            :precision="2"
            :min="0"
            :step="1000"
            style="width: 100%"
            @change="calculatePremium"
          />
        </el-form-item>
        <el-form-item label="年保费">
          <div class="calculated-premium">¥{{ calculatedAnnualPremium }}</div>
          <div class="premium-hint">自动计算：{{ form.baseSalary }} × {{ form.rate }} = {{ calculatedAnnualPremium }}</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import {
  getPremiumConfigList,
  getPremiumConfigById,
  addPremiumConfig,
  updatePremiumConfig,
  deletePremiumConfig
} from '@/api/premiumConfig'

const userInfo = ref(JSON.parse(sessionStorage.getItem('user') || '{}'))
const isAdmin = computed(() => userInfo.value.roleId === 1)

const searchForm = reactive({
  jobType: null,
  status: null
})

const tableData = ref([])
const loading = ref(false)

const filteredTableData = computed(() => {
  return tableData.value.filter(item => {
    const jobTypeMatch = !searchForm.jobType || item.jobType === searchForm.jobType
    const statusMatch = searchForm.status === null || searchForm.status === undefined || item.status === searchForm.status
    return jobTypeMatch && statusMatch
  })
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增保费配置')
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  jobType: null,
  rate: 0.015,
  baseSalary: 120000,
  annualPremium: 0,
  status: 1
})

const formRules = {
  jobType: [{ required: true, message: '请选择工种', trigger: 'change' }],
  rate: [{ required: true, message: '请输入费率', trigger: 'blur' }],
  baseSalary: [{ required: true, message: '请输入基数', trigger: 'blur' }]
}

const calculatedAnnualPremium = computed(() => {
  const premium = form.baseSalary * form.rate
  return isNaN(premium) ? '0.00' : premium.toFixed(2)
})

const calculatePremium = () => {
  form.annualPremium = parseFloat(calculatedAnnualPremium.value)
}

const getJobTypeTag = (jobType) => {
  const map = { '一类': 'success', '二类': 'warning', '三类': 'danger' }
  return map[jobType] || ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPremiumConfigList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
}

const handleReset = () => {
  searchForm.jobType = null
  searchForm.status = null
}

const handleAdd = () => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以添加保费配置')
    return
  }
  isEdit.value = false
  dialogTitle.value = '新增保费配置'
  resetForm()
  calculatePremium()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以编辑保费配置')
    return
  }
  isEdit.value = true
  dialogTitle.value = '编辑保费配置'
  try {
    const res = await getPremiumConfigById(row.id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取配置信息失败')
  }
}

const handleDelete = (row) => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以删除保费配置')
    return
  }
  ElMessageBox.confirm(`确定要删除工种【${row.jobType}】的保费配置吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deletePremiumConfig(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以保存保费配置')
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    form.annualPremium = parseFloat(calculatedAnnualPremium.value)
    if (isEdit.value) {
      await updatePremiumConfig(form)
      ElMessage.success('更新成功')
    } else {
      await addPremiumConfig(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.jobType = null
  form.rate = 0.015
  form.baseSalary = 120000
  form.annualPremium = 0
  form.status = 1
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.premium-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.premium-amount {
  color: #f56c6c;
  font-weight: bold;
}

.formula-note {
  margin-top: 20px;
}

.rate-hint,
.premium-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.calculated-premium {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
