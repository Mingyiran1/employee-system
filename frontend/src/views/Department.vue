<template>
  <div class="department-container">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增部门</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="name" label="部门名称" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getDepartmentList, addDepartment, updateDepartment, deleteDepartment } from '@/api/department'

const tableData = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  sortOrder: 0
})

const formRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentList()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增部门'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑部门'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除部门【${row.name}】吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateDepartment(form)
      ElMessage.success('更新成功')
    } else {
      await addDepartment(form)
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
  form.sortOrder = 0
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
