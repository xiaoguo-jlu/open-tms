<template>
  <div class="base-data-picker">
    <el-input
      v-model="displayText"
      :placeholder="placeholder || '请选择'"
      :disabled="disabled"
      :clearable="clearable"
      :title="displayText"
      readonly
      @clear="handleClear"
      @click="openDialog"
    >
      <template #append>
        <el-button :icon="Search" :disabled="disabled" @click="openDialog" />
      </template>
    </el-input>

    <el-dialog
      v-model="dialogVisible"
      :title="`选择${preset.label}`"
      width="900px"
      :close-on-click-modal="false"
      append-to-body
      @close="handleClose"
    >
      <div class="picker-toolbar">
        <el-input
          v-model="searchKeyword"
          :placeholder="searchPlaceholder"
          clearable
          style="width: 280px;"
          @input="onSearchInput"
          @clear="onSearchInput"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <span class="picker-hint" v-if="preset.searchFields?.length">
          可搜索: {{ preset.searchFields.join(' / ') }}
        </span>
      </div>

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        highlight-current-row
        stripe
        border
        max-height="400"
        style="margin-top: 12px;"
        @row-click="handleRowClick"
        @row-dblclick="handleRowClick"
      >
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column
          v-for="col in preset.columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          show-overflow-tooltip
        />
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 12px; justify-content: flex-end;"
        @current-change="loadData"
        @size-change="loadData"
      />

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCurrent" :disabled="!currentRow">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * BaseDataPicker — 通用基础数据选择器(单选)
 *
 * Props:
 *   modelValue  绑定值(返回字段由 preset.returnField 决定,默认 id)
 *   entity      preset key,如 'bank-account' / 'currency'
 *   placeholder 占位文本
 *   disabled    是否禁用
 *   clearable   是否可清空(默认 true)
 *   filters     固定过滤条件,合并到每次查询
 *   autoFilter  响应式过滤条件(对象),变更时自动重新加载(并回到第 1 页)
 *
 * Events:
 *   update:modelValue  绑定值变化
 *   change             (row) => void  选中行后(行对象或 null)
 *   clear              ()  => void    清空时
 *   open               ()  => void    打开弹窗时
 *   close              ()  => void    关闭弹窗时
 */
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPreset } from './pickerPresets.js'

const props = defineProps({
  modelValue: { type: [Number, String], default: null },
  entity: { type: String, required: true },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  clearable: { type: Boolean, default: true },
  filters: { type: Object, default: () => ({}) },
  autoFilter: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'change', 'clear', 'open', 'close'])

const preset = computed(() => getPreset(props.entity))

const dialogVisible = ref(false)
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const currentRow = ref(null)

/* ============ 显示文本 ============ */
const displayText = ref('')

function rebuildDisplayText() {
  const v = props.modelValue
  if (v == null || v === '') {
    displayText.value = ''
    return
  }
  // 命中当前页缓存
  const hit = tableData.value.find((r) => r[preset.value.returnField] === v)
  if (hit) {
    displayText.value = preset.value.displayFormat(hit)
    return
  }
  // 命中已确认的行缓存
  if (currentRow.value && currentRow.value[preset.value.returnField] === v) {
    displayText.value = preset.value.displayFormat(currentRow.value)
    return
  }
  // 默认显示原始值
  displayText.value = String(v)
}

async function fetchByIdIfNeeded() {
  const v = props.modelValue
  if (v == null || v === '') {
    displayText.value = ''
    return
  }
  // 已经在缓存中
  if (tableData.value.some((r) => r[preset.value.returnField] === v)) {
    rebuildDisplayText()
    return
  }
  if (currentRow.value && currentRow.value[preset.value.returnField] === v) {
    rebuildDisplayText()
    return
  }
  // 调 listApi 第 1 页搜这个值
  try {
    loading.value = true
    const params = mergeParams({ pageNum: 1, pageSize: 20, keyword: String(v) })
    const { records } = await preset.value.listApi(params)
    const row = records.find((r) => r[preset.value.returnField] === v) || records[0]
    if (row) {
      currentRow.value = row
      displayText.value = preset.value.displayFormat(row)
    } else {
      displayText.value = String(v)
    }
  } catch (e) {
    displayText.value = String(v)
  } finally {
    loading.value = false
  }
}

const searchPlaceholder = computed(() => {
  const fields = preset.value.searchFields || []
  if (fields.length === 0) return '请输入关键字搜索'
  return `搜索 ${fields.join(' / ')}`
})

/* ============ 加载数据 ============ */
function mergeParams(extra = {}) {
  return {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: searchKeyword.value || undefined,
    ...(preset.value.defaultFilters || {}),
    ...(props.filters || {}),
    ...(props.autoFilter || {}),
    ...extra
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = mergeParams()
    const { records, total: t } = await preset.value.listApi(params)
    tableData.value = records
    total.value = t
    // 高亮当前已选项
    nextTick(() => {
      const v = props.modelValue
      if (v != null && v !== '') {
        const idx = tableData.value.findIndex((r) => r[preset.value.returnField] === v)
        if (idx >= 0) {
          currentRow.value = tableData.value[idx]
          tableRef.value?.setCurrentRow(tableData.value[idx])
        }
      }
    })
  } catch (e) {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/* ============ 搜索(防抖) ============ */
let searchTimer = null
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pageNum.value = 1
    loadData()
  }, 300)
}

/* ============ 弹窗 ============ */
const tableRef = ref(null)
function openDialog() {
  if (props.disabled) return
  dialogVisible.value = true
  emit('open')
  // 重置搜索
  searchKeyword.value = ''
  pageNum.value = 1
  loadData()
}

function handleClose() {
  emit('close')
}

function handleRowClick(row) {
  currentRow.value = row
  tableRef.value?.setCurrentRow(row)
}

function confirmCurrent() {
  if (!currentRow.value) {
    ElMessage.warning('请先选择一行')
    return
  }
  selectRow(currentRow.value)
}

function handleClear() {
  currentRow.value = null
  displayText.value = ''
  emit('update:modelValue', null)
  emit('change', null)
  emit('clear')
}

function selectRow(row) {
  currentRow.value = row
  displayText.value = preset.value.displayFormat(row)
  const val = row[preset.value.returnField]
  emit('update:modelValue', val)
  emit('change', row)
  dialogVisible.value = false
}

/* ============ 监听外部 v-model 变化 ============ */
watch(
  () => props.modelValue,
  () => {
    fetchByIdIfNeeded()
  }
)

/* ============ 监听自动过滤(深) ============ */
watch(
  () => props.autoFilter,
  () => {
    pageNum.value = 1
    if (dialogVisible.value) loadData()
  },
  { deep: true }
)

/* ============ 监听固定过滤(深) ============ */
watch(
  () => props.filters,
  () => {
    pageNum.value = 1
    if (dialogVisible.value) loadData()
  },
  { deep: true }
)

/* ============ 初始化:显示文本 ============ */
onMounted(() => {
  fetchByIdIfNeeded()
})
</script>

<style scoped>
.base-data-picker {
  width: 100%;
}
.picker-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.picker-hint {
  color: #909399;
  font-size: 12px;
}
</style>