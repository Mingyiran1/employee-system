<template>
  <div class="message-container">
    <el-card class="message-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">消息通知</span>
            <el-tag v-if="unreadCount > 0" type="danger" size="small">{{ unreadCount }} 条未读</el-tag>
          </div>
          <div class="header-right">
            <el-button
              v-if="unreadCount > 0"
              type="primary"
              link
              :icon="Check"
              @click="handleMarkAllRead"
            >
              全部已读
            </el-button>
            <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="unread">未读</el-radio-button>
              <el-radio-button label="read">已读</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <!-- 消息列表 -->
      <div v-if="messageList.length > 0" class="message-list">
        <div
          v-for="msg in messageList"
          :key="msg.id"
          :class="['message-item', { 'unread': msg.isRead === 0 }]"
          @click="handleClickMessage(msg)"
        >
          <div class="message-icon">
            <el-avatar
              :size="40"
              :icon="getMessageIcon(msg.type)"
              :style="{ backgroundColor: getMessageColor(msg.type) }"
            />
          </div>
          <div class="message-content">
            <div class="message-title-row">
              <span class="message-title">{{ msg.title }}</span>
              <el-tag v-if="msg.isRead === 0" type="danger" size="small" effect="plain">未读</el-tag>
            </div>
            <div class="message-body">{{ msg.content }}</div>
            <div class="message-footer">
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
              <div class="message-actions">
                <el-button
                  v-if="msg.isRead === 0"
                  type="primary"
                  link
                  size="small"
                  @click.stop="handleMarkRead(msg)"
                >
                  标记已读
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click.stop="handleDelete(msg)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="暂无消息" />

      <!-- 分页 -->
      <el-pagination
        v-if="pageInfo.total > 0"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Bell, DocumentChecked, Warning, InfoFilled } from '@element-plus/icons-vue'
import { getMessageList, markAsRead, markAllAsRead, getUnreadCount } from '@/api/message'
import eventBus from '@/utils/eventBus'

const router = useRouter()

// 数据
const messageList = ref([])
const unreadCount = ref(0)
const filterType = ref('all')
const loading = ref(false)

const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 消息类型对应的图标
const getMessageIcon = (type) => {
  const iconMap = {
    'approval': DocumentChecked,
    'system': Warning,
    'notice': InfoFilled,
    'default': Bell
  }
  return iconMap[type] || iconMap['default']
}

// 消息类型对应的颜色
const getMessageColor = (type) => {
  const colorMap = {
    'approval': '#409EFF',
    'system': '#E6A23C',
    'notice': '#67C23A',
    'default': '#909399'
  }
  return colorMap[type] || colorMap['default']
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 1分钟内
  if (diff < 60000) {
    return '刚刚'
  }
  // 1小时内
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }
  // 24小时内
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }
  // 7天内
  if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前'
  }
  // 超过7天显示具体日期
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取未读消息总数
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    console.error('获取未读消息数失败', error)
  }
}

// 获取消息列表
const fetchMessages = async () => {
  loading.value = true
  try {
    const res = await getMessageList({
      page: pageInfo.page,
      size: pageInfo.size
    })

    let records = res.data.records || []

    // 前端过滤已读/未读
    if (filterType.value === 'unread') {
      records = records.filter(item => item.isRead === 0)
    } else if (filterType.value === 'read') {
      records = records.filter(item => item.isRead === 1)
    }

    messageList.value = records
    pageInfo.total = res.data.total || 0

    // 获取真实的未读总数（不是当前页的）
    await fetchUnreadCount()
  } finally {
    loading.value = false
  }
}

// 筛选切换
const handleFilterChange = () => {
  pageInfo.page = 1
  fetchMessages()
}

// 分页
const handleSizeChange = (val) => {
  pageInfo.size = val
  fetchMessages()
}

const handlePageChange = (val) => {
  pageInfo.page = val
  fetchMessages()
}

// 点击消息
const handleClickMessage = (msg) => {
  // 如果有相关业务ID，跳转到对应页面
  if (msg.businessType === 'APPROVAL' && msg.businessId) {
    router.push('/approval')
  }

  // 标记为已读（不调用handleMarkRead避免重复emit）
  if (msg.isRead === 0) {
    markAsRead(msg.id).then(() => {
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      // 通知 Layout 组件刷新未读数
      eventBus.emit('message-read')
    }).catch(error => {
      console.error('标记已读失败', error)
    })
  }
}

// 标记单条已读
const handleMarkRead = async (msg) => {
  try {
    await markAsRead(msg.id)
    msg.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('已标记为已读')
    // 通知 Layout 组件刷新未读数
    eventBus.emit('message-read')
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 标记全部已读
const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定要将所有消息标记为已读吗？', '确认', {
      type: 'warning'
    })

    const res = await markAllAsRead()
    const count = res.data || 0

    // 刷新列表
    messageList.value.forEach(msg => {
      msg.isRead = 1
    })
    unreadCount.value = 0

    // 通知 Layout 组件刷新未读数
    eventBus.emit('message-read')

    ElMessage.success(`已将 ${count} 条消息标记为已读`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记全部已读失败', error)
    }
  }
}

// 删除消息（前端模拟，后端可能没有删除接口）
const handleDelete = (msg) => {
  ElMessageBox.confirm('确定要删除这条消息吗？', '确认删除', {
    type: 'warning'
  }).then(() => {
    // 前端删除（实际应该调用后端接口）
    const index = messageList.value.findIndex(item => item.id === msg.id)
    if (index > -1) {
      messageList.value.splice(index, 1)
      pageInfo.total = Math.max(0, pageInfo.total - 1)
      if (msg.isRead === 0) {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    }
    ElMessage.success('已删除')
  }).catch(() => {})
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.message-container {
  padding: 20px;
}

.message-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background-color: #f5f7fa;
}

.message-item {
  display: flex;
  padding: 16px 20px;
  background-color: #fff;
  cursor: pointer;
  transition: background-color 0.3s;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.unread {
  background-color: #f0f9ff;
}

.message-item.unread:hover {
  background-color: #e6f7ff;
}

.message-icon {
  margin-right: 15px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.message-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.message-item.unread .message-title {
  font-weight: bold;
}

.message-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-actions {
  display: flex;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.message-item:hover .message-actions {
  opacity: 1;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
