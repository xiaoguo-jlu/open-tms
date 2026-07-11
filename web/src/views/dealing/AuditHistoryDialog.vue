<template>
  <el-dialog
    :model-value="visible"
    title="审计历史"
    width="800px"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="(v) => emit('update:visible', v)"
    @open="onOpen"
    @close="onClose"
  >
    <div class="audit-history-dialog">
      <!-- 顶部 deal 标识 -->
      <div class="deal-bar">
        <span class="deal-label">交易编号</span>
        <span class="deal-value mono">{{ dealNumber }}</span>
      </div>

      <!-- 类型筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="filterImageType" size="small" @change="reload">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="CREATE">CREATE</el-radio-button>
          <el-radio-button value="UPDATE">UPDATE</el-radio-button>
          <el-radio-button value="DELETE">DELETE</el-radio-button>
          <el-radio-button value="RATE_FIX">RATE_FIX</el-radio-button>
          <el-radio-button value="STATUS_CHANGE">STATUS_CHANGE</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 表格 -->
      <el-table
        :data="pagedRecords"
        v-loading="loading"
        stripe
        size="small"
        class="version-table"
        :empty-text="loading ? '加载中…' : '暂无历史版本'"
        max-height="420"
      >
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="version" label="版本" width="70" align="center">
          <template #default="{ row }">
            <span class="mono">V{{ row.version }}</span>
          </template>
        </el-table-column>
        <el-table-column label="镜像类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getImageTypeTag(row.imageType)" size="small">{{ row.imageType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="operateAt" label="操作时间" width="170" />
        <el-table-column prop="changeSummary" label="变更摘要" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="onSelect(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && records.length === 0"
        description="该交易暂无历史版本记录"
        :image-size="60"
        style="margin-top: 12px"
      />

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        small
        style="margin-top: 12px; justify-content: flex-end;"
        @current-change="reload"
        @size-change="reload"
      />
    </div>

    <template #footer>
      <el-button @click="emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { listVersions } from '@/api/dealing/auditHistory'

const props = defineProps({
  visible: { type: Boolean, default: false },
  dealNumber: { type: String, default: '' }
})

const emit = defineEmits(['update:visible', 'select'])

// 列表状态
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const filterImageType = ref('')

const pagedRecords = computed(() => records.value || [])

const getImageTypeTag = (t) => {
  const map = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    RATE_FIX: 'info',
    STATUS_CHANGE: 'primary'
  }
  return map[t] || 'info'
}

const reload = async () => {
  if (!props.dealNumber) return
  loading.value = true
  try {
    const res = await listVersions(props.dealNumber, {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      imageType: filterImageType.value || undefined
    })
    const d = res?.data || {}
    records.value = d.records || d.list || []
    total.value = d.total || records.value.length || 0
  } catch (e) {
    records.value = []
    total.value = 0
    ElMessage.error(e?.message || '加载版本列表失败')
  } finally {
    loading.value = false
  }
}

const onOpen = () => {
  pageNum.value = 1
  filterImageType.value = ''
  reload()
}

const onClose = () => {
  records.value = []
  total.value = 0
  pageNum.value = 1
}

const onSelect = (row) => {
  if (!row || row.version == null) return
  emit('select', row.version)
  emit('update:visible', false)
}

// dealNumber 变化时自动刷新(支持动态传入)
watch(() => props.dealNumber, (v) => {
  if (props.visible && v) reload()
})
</script>

<style scoped>
.audit-history-dialog { padding: 0 4px; }

.deal-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%);
  border: 1px solid #d9ecff;
  border-radius: 4px;
}
.deal-label { font-size: 12px; color: #909399; }
.deal-value { font-size: 13px; font-weight: 600; color: #303133; }

.filter-bar { margin-bottom: 12px; display: flex; justify-content: flex-start; }

.version-table :deep(.el-table__row) { height: 38px; }
.version-table :deep(.el-table__cell) { padding: 4px 0; font-size: 12px; }

.mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }
</style>