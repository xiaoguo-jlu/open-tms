<template>
  <el-tag :type="badgeType" effect="dark" size="small" v-if="mode && mode !== 'readonly'">
    <el-icon v-if="iconName">
      <component :is="iconName" />
    </el-icon>
    {{ label }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { Edit, Plus, DocumentCopy } from '@element-plus/icons-vue'

const props = defineProps({
  mode: { type: String, default: '' },
  copyFrom: { type: String, default: '' }
})

const badgeType = computed(() => {
  if (props.mode === 'edit') return 'primary'
  if (props.mode === 'new') return 'success'
  if (props.mode === 'copy') return 'warning'
  return 'info'
})

const iconName = computed(() => {
  if (props.mode === 'edit') return Edit
  if (props.mode === 'new') return Plus
  if (props.mode === 'copy') return DocumentCopy
  return null
})

const label = computed(() => {
  if (props.mode === 'edit') return '编辑中'
  if (props.mode === 'new') return '新建'
  if (props.mode === 'copy') return `复制自 ${props.copyFrom || ''}`
  return ''
})
</script>
