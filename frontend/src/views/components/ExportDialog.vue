<template>
  <el-dialog
    v-model="visible"
    title="导出员工报表"
    width="500px"
    destroy-on-close
  >
    <div class="export-dialog">
      <!-- 导出范围 -->
      <div class="section">
        <div class="section-title">导出范围</div>
        <el-radio-group v-model="form.exportScope">
          <el-radio :label="1">当前页数据（{{ currentPageCount }}条）</el-radio>
          <el-radio :label="2">所有筛选数据（{{ totalCount }}条）</el-radio>
        </el-radio-group>
      </div>

      <!-- 字段选择 -->
      <div class="section">
        <div class="section-title">
          选择字段
          <el-link type="primary" :underline="false" @click="selectAll">
            全选
          </el-link>
          <el-link type="info" :underline="false" @click="selectDefault">
            默认
          </el-link>
          <el-link type="danger" :underline="false" @click="clearAll">
            清空
          </el-link>
        </div>
        <el-checkbox-group v-model="form.fields" class="field-group">
          <el-checkbox
            v-for="field in fieldOptions"
            :key="field.value"
            :label="field.value"
            class="field-checkbox"
          >
            {{ field.label }}
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 提示信息 -->
      <el-alert
        v-if="totalCount > 5000"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #title>
          数据量较大（{{ totalCount }}条），导出可能需要较长时间，请耐心等待
        </template>
      </el-alert>

      <el-alert
        v-if="totalCount > 10000"
        type="error"
        :closable="false"
        show-icon
      >
        <template #title>
          数据量过大（{{ totalCount }}条），请使用筛选条件缩小范围（最多支持10000条）
        </template>
      </el-alert>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        :disabled="!canExport"
        @click="handleExport"
      >
        {{ submitText }}
      </el-button>
    </template>
  </el-dialog>

  <!-- 导出进度弹窗 -->
  <el-dialog
    v-model="progressVisible"
    title="导出中..."
    width="400px"
    :close-on-click-modal="false"
    :show-close="false"
  >
    <div class="progress-content">
      <el-progress
        :percentage="progressPercent"
        :status="progressStatus"
        :stroke-width="18"
        striped
        striped-flow
      />
      <p class="progress-text">{{ progressText }}</p>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getExportFields, createExportTask, getTaskStatus, downloadExportFile } from '@/api/export'

const props = defineProps({
  modelValue: Boolean,
  currentPageCount: { type: Number, default: 0 },
  totalCount: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  searchParams: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单数据
const form = reactive({
  exportScope: 1,
  fields: []
})

const fieldOptions = ref([])
const submitting = ref(false)
const progressVisible = ref(false)
const progressPercent = ref(0)
const progressStatus = ref('')
const progressText = ref('正在准备导出...')
const currentTaskId = ref(null)

// 计算属性
const canExport = computed(() => {
  return form.fields.length > 0 && props.totalCount > 0 && props.totalCount <= 10000
})

const submitText = computed(() => {
  if (props.totalCount > 5000) {
    return '确认导出（可能需要较长时间）'
  }
  return '确认导出'
})

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val) {
    loadFieldOptions()
    form.exportScope = 1
    // 默认选中默认字段
    selectDefault()
  }
})

// 加载字段选项
const loadFieldOptions = async () => {
  try {
    const res = await getExportFields()
    fieldOptions.value = res.data
  } catch (error) {
    console.error('加载字段选项失败', error)
  }
}

// 全选
const selectAll = () => {
  form.fields = fieldOptions.value.map(f => f.value)
}

// 选择默认
const selectDefault = () => {
  form.fields = fieldOptions.value
    .filter(f => f.defaultChecked)
    .map(f => f.value)
}

// 清空
const clearAll = () => {
  form.fields = []
}

// 执行导出
const handleExport = async () => {
  if (form.fields.length === 0) {
    ElMessage.warning('请至少选择一个字段')
    return
  }

  submitting.value = true
  try {
    // 构建导出参数
    const params = {
      exportScope: form.exportScope,
      fields: form.fields,
      page: props.page,
      size: props.size,
      ...props.searchParams
    }

    // 创建导出任务
    const res = await createExportTask(params)
    currentTaskId.value = res.data

    // 关闭配置弹窗，显示进度
    visible.value = false
    progressVisible.value = true
    progressPercent.value = 0
    progressStatus.value = ''
    progressText.value = '正在导出数据，请稍候...'

    // 轮询任务状态
    startPolling(res.data)

  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '创建导出任务失败')
  } finally {
    submitting.value = false
  }
}

// 轮询任务状态
const startPolling = (taskId) => {
  const pollInterval = 1000 // 每秒轮询一次
  const maxAttempts = 300 // 最多轮询5分钟
  let attempts = 0

  const poll = async () => {
    attempts++
    if (attempts > maxAttempts) {
      progressVisible.value = false
      ElMessage.error('导出超时，请稍后到消息中心查看结果')
      return
    }

    try {
      const res = await getTaskStatus(taskId)
      const task = res.data

      switch (task.status) {
        case 0: // 等待中
          progressPercent.value = 5
          progressText.value = '等待执行...'
          setTimeout(poll, pollInterval)
          break
        case 1: // 执行中
          progressPercent.value = Math.min(50 + (attempts * 2), 95)
          progressText.value = `正在导出数据...（共${task.totalCount}条）`
          setTimeout(poll, pollInterval)
          break
        case 2: // 成功
          progressPercent.value = 100
          progressStatus.value = 'success'
          progressText.value = '导出完成！'

          // 延迟关闭进度弹窗并下载
          setTimeout(() => {
            progressVisible.value = false
            downloadFile(taskId, task.fileName)
            ElMessage.success({
              message: `报表导出完成！共${task.totalCount}条记录`,
              duration: 5000
            })
            emit('success')
          }, 1000)
          break
        case 3: // 失败
          progressVisible.value = false
          progressStatus.value = 'exception'
          ElMessage.error(`导出失败：${task.errorMsg || '未知错误'}`)
          break
        default:
          setTimeout(poll, pollInterval)
      }
    } catch (error) {
      console.error('轮询任务状态失败', error)
      setTimeout(poll, pollInterval)
    }
  }

  poll()
}

// 下载文件
const downloadFile = async (taskId, fileName) => {
  try {
    const blob = await downloadExportFile(taskId)
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
  } catch (error) {
    ElMessage.error('文件下载失败')
  }
}
</script>

<style scoped>
.export-dialog {
  padding: 10px 0;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-weight: bold;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.field-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.field-checkbox {
  min-width: 100px;
}

.progress-content {
  padding: 20px 0;
}

.progress-text {
  text-align: center;
  margin-top: 16px;
  color: #606266;
}
</style>
