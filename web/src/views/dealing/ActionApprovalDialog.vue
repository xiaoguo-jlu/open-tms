<template>
  <el-dialog v-model="visible" title="审批 Action（v2.0 - 审批不改变 DealMap/Cashflow）" width="800px" @close="handleClose">
    <el-alert type="warning" :closable="false" style="margin-bottom: 12px;">
      <template #title>请选择需要审批的 Action（可多选）。审批仅更新 Action 状态，DealMap / Cashflow 任何状态都不变。</template>
    </el-alert>

    <el-table :data="pendingActions" @selection-change="onSelectionChange" stripe>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="actionNumber" label="Action 编号" width="160" />
      <el-table-column prop="actionType" label="类型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="getActionTypeTag(row.actionType)" size="small">{{ getActionTypeLabel(row.actionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="operateAt" label="操作时间" width="170" />
      <el-table-column prop="approvalStatus1" label="当前状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getApprovalTag(row.approvalStatus1)" size="small">{{ row.approvalStatus1 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
    </el-table>

    <el-form :model="form" label-width="100px" style="margin-top: 16px;">
      <el-form-item label="审批人">
        <el-input v-model="form.approver" placeholder="审批人" />
      </el-form-item>
      <el-form-item label="审批意见" :required="isReject">
        <el-input v-model="form.approvalRemark" type="textarea" :rows="2" :placeholder="isReject ? '驳回时审批意见必填' : '可选'" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="danger" :disabled="selectedActions.length === 0" :loading="rejecting" @click="handleReject">驳回</el-button>
      <el-button type="primary" :disabled="selectedActions.length === 0" :loading="approving" @click="handleApprove">审批通过</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { approveAction, rejectAction } from '@/api/dealing/acDeal'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  deal: { type: Object, default: () => ({}) },
  actions: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue', 'approved'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, (v) => { visible.value = v })
watch(visible, (v) => emit('update:modelValue', v))

const form = reactive({ approver: 'manager01', approvalRemark: '' })
const selectedActions = ref([])
const approving = ref(false)
const rejecting = ref(false)
const isReject = ref(false)

const pendingActions = computed(() => (props.actions || []).filter(a => a.approvalStatus1 === 'Pending'))

const getActionTypeLabel = (t) => ({ CREATE: '创建', UPDATE: '修改', DELETE: '删除', APPROVE: '审批', REJECT: '驳回' }[t] || t)
const getActionTypeTag = (t) => ({ CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', APPROVE: 'success', REJECT: 'danger' }[t] || 'info')
const getApprovalTag = (s) => ({ Approved: 'success', Pending: 'warning', Rejected: 'danger' }[s] || 'info')

const onSelectionChange = (rows) => { selectedActions.value = rows }

const handleClose = () => { visible.value = false; isReject.value = false; form.approvalRemark = ''; selectedActions.value = [] }

const handleApprove = async () => {
  if (!form.approver) { ElMessage.error('审批人不能为空'); return }
  approving.value = true
  try {
    for (const action of selectedActions.value) {
      await approveAction(action.actionNumber, { approver: form.approver, approvalRemark: form.approvalRemark })
    }
    ElMessage.success(`已审批 ${selectedActions.value.length} 个 Action`)
    emit('approved')
    handleClose()
  } catch (e) {
    ElMessage.error(e?.message || '审批失败')
  } finally {
    approving.value = false
  }
}

const handleReject = async () => {
  isReject.value = true
  if (!form.approver) { ElMessage.error('审批人不能为空'); return }
  if (!form.approvalRemark) { ElMessage.error('驳回时审批意见必填'); return }
  rejecting.value = true
  try {
    for (const action of selectedActions.value) {
      await rejectAction(action.actionNumber, { approver: form.approver, approvalRemark: form.approvalRemark })
    }
    ElMessage.success(`已驳回 ${selectedActions.value.length} 个 Action`)
    emit('approved')
    handleClose()
  } catch (e) {
    ElMessage.error(e?.message || '驳回失败')
  } finally {
    rejecting.value = false
  }
}
</script>
