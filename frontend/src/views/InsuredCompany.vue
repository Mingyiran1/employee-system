<template>
  <div class="insured-company-container">
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="公司名称">
          <el-input v-model="searchForm.name" placeholder="请输入公司名称" clearable />
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

    <el-card class="table-card">
      <div class="toolbar">
        <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="handleAdd">新增公司</el-button>
        <el-button v-if="isAdmin" type="danger" :icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column v-if="isAdmin" type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="公司名称" min-width="150" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="employeeCount" label="员工数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.employeeCount > 0 ? 'primary' : 'info'">{{ row.employeeCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isAdmin" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="isAdmin" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
            <span v-if="!isAdmin" style="color: #909399; font-size: 12px;">仅查看</span>
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
        <el-form-item label="公司名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="公司地址" prop="address">
          <el-input v-model="form.address" type="textarea" rows="2" placeholder="请输入公司地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
import { getCompanyPage, addCompany, updateCompany, deleteCompany } from '@/api/insuranceCompany'

// 获取当前用户信息判断权限
const userInfo = ref(JSON.parse(sessionStorage.getItem('user') || '{}'))
const isAdmin = computed(() => userInfo.value.roleId === 1)

const searchForm = reactive({
  name: '',
  status: null
})

const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增投保公司')
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  contactName: '',
  contactPhone: '',
  address: '',
  status: 1,
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCompanyPage({
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

const handleSearch = () => {
  pageInfo.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = null
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAdd = () => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以添加投保公司')
    return
  }
  isEdit.value = false
  dialogTitle.value = '新增投保公司'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以编辑投保公司')
    return
  }
  isEdit.value = true
  dialogTitle.value = '编辑投保公司'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以删除投保公司')
    return
  }
  // 检查是否有员工关联
  if (row.employeeCount > 0) {
    ElMessage.warning('该公司下存在员工，无法删除')
    return
  }
  ElMessageBox.confirm(`确定要删除投保公司【${row.name}】吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteCompany(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleBatchDelete = () => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以删除投保公司')
    return
  }
  // 检查选中的公司是否有员工
  const selectedCompanies = tableData.value.filter(item => selectedIds.value.includes(item.id))
  const hasEmployees = selectedCompanies.some(item => item.employeeCount > 0)
  if (hasEmployees) {
    ElMessage.warning('选中的公司中存在有员工的公司，无法删除')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 家投保公司吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    for (const id of selectedIds.value) {
      await deleteCompany(id)
    }
    ElMessage.success('批量删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCompany(form)
      ElMessage.success('更新成功')
    } else {
      await addCompany(form)
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
  form.name = ''
  form.contactName = ''
  form.contactPhone = ''
  form.address = ''
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
})
</script>

<style scoped>
.insured-company-container {
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
