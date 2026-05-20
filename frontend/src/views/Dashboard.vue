<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon total">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">总员工数</div>
            <div class="stat-value">{{ overview.totalEmployees || 0 }}人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon new">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月新增</div>
            <div class="stat-value positive">+{{ overview.newThisMonth || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon leave">
            <el-icon><Minus /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月离职</div>
            <div class="stat-value negative">-{{ overview.leaveThisMonth || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon pending">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待审批</div>
            <div class="stat-value warning">{{ overview.pendingApproval || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中间图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>部门分布</span>
            </div>
          </template>
          <v-chart class="chart" :option="deptChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>性别比例</span>
            </div>
          </template>
          <v-chart class="chart" :option="genderChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部趋势图 -->
    <el-row class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>入职趋势（近12个月）</span>
            </div>
          </template>
          <v-chart class="trend-chart" :option="trendChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { User, Plus, Minus, DocumentChecked } from '@element-plus/icons-vue'
import { getDashboardAll } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 数据
const overview = ref({
  totalEmployees: 0,
  newThisMonth: 0,
  leaveThisMonth: 0,
  pendingApproval: 0
})

const deptDistribution = ref([])
const genderDistribution = ref([])
const entryTrend = ref({
  months: [],
  counts: []
})

// 部门分布图表配置
const deptChartOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c}人 ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center'
  },
  color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8E44AD', '#16A085'],
  series: [
    {
      name: '部门分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }
  ]
})

// 性别分布图表配置
const genderChartOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c}人 ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center'
  },
  color: ['#409EFF', '#F56C6C'],
  series: [
    {
      name: '性别比例',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }
  ]
})

// 入职趋势图表配置
const trendChartOption = ref({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [],
    axisLine: {
      lineStyle: {
        color: '#909399'
      }
    },
    axisLabel: {
      show: true,
      rotate: 45,
      interval: 0
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      lineStyle: {
        color: '#909399'
      }
    },
    splitLine: {
      lineStyle: {
        color: '#EBEEF5'
      }
    }
  },
  series: [
    {
      name: '入职人数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      sampling: 'average',
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ]
        }
      },
      data: []
    }
  ]
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    const res = await getDashboardAll()
    const data = res.data || {}

    // 概览数据 - 适配后端返回的 employeeOverview 结构
    if (data.employeeOverview) {
      overview.value = {
        totalEmployees: data.employeeOverview.totalCount || 0,
        newThisMonth: data.employeeOverview.newThisMonthCount || 0,
        leaveThisMonth: data.employeeOverview.resignedThisMonthCount || 0,
        pendingApproval: data.pendingApprovalCount || 0
      }
    }

    // 部门分布数据 - 适配后端的 deptDistribution 结构
    if (data.deptDistribution) {
      deptDistribution.value = data.deptDistribution.map(item => ({
        name: item.deptName,
        value: item.employeeCount
      }))
      deptChartOption.value.series[0].data = deptDistribution.value
    }

    // 性别分布数据 - 适配后端的 genderDistribution 结构
    if (data.genderDistribution) {
      const maleCount = data.genderDistribution.maleCount || 0
      const femaleCount = data.genderDistribution.femaleCount || 0
      // 始终显示男女两项，即使值为0
      genderDistribution.value = [
        { name: '男', value: maleCount },
        { name: '女', value: femaleCount }
      ]
      genderChartOption.value.series[0].data = [...genderDistribution.value]
    }

    // 入职趋势数据 - 处理最近12个月
    if (data.entryTrend) {
      // 生成最近12个月的月份列表（包含当前月）
      const months = []
      const now = new Date()
      for (let i = 11; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
        const monthStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
        months.push(monthStr)
      }

      // 将后端数据转换为Map
      const countMap = {}
      data.entryTrend.forEach(item => {
        countMap[item.month] = item.count
      })

      // 填充数据，缺失的月份设为0
      const counts = months.map(month => countMap[month] || 0)

      entryTrend.value = { months, counts }
      trendChartOption.value.xAxis.data = months
      trendChartOption.value.series[0].data = counts
    }
  } catch (error) {
    ElMessage.error('获取仪表盘数据失败')
    console.error('获取仪表盘数据失败', error)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon .el-icon {
  font-size: 28px;
  color: #fff;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.new {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.leave {
  background: linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-value.positive {
  color: #67C23A;
}

.stat-value.negative {
  color: #F56C6C;
}

.stat-value.warning {
  color: #E6A23C;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-card :deep(.el-card__body) {
  height: calc(100% - 60px);
  padding: 10px;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart {
  width: 100%;
  height: 100%;
}

.trend-chart {
  width: 100%;
  height: 100%;
}
</style>
