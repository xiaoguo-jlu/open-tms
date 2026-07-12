<template>
  <div class="fx-deal-detail one-screen">
    <!-- 顶部工具条 -->
    <div class="action-bar top">
      <div class="left">
        <el-button :icon="ArrowLeft" size="small" @click="handleBack">返回</el-button>
        <span class="page-title">FX 交易详情 - {{ detail.dealNumber || (mode === 'new' ? '新建' : '复制') }}</span>
        <ModeBadge :mode="mode" :copy-from="route.query.copyFrom" />
      </div>
      <div class="right">
        <template v-if="mode === 'readonly'">
          <el-button v-if="isNdfForDetail && !detail.fixingRate" type="warning" size="small" :icon="MagicStick" @click="openRateFixDialog">RATE_FIX</el-button>
          <el-button type="success" size="small" :icon="CopyDocument" @click="enterCopy">复制</el-button>
          <el-button v-if="detail.status !== 'Canceled'" type="primary" size="small" :icon="Edit" @click="enterEdit">编辑</el-button>
          <el-button v-if="detail.status === 'New' || detail.status === 'Pending'" type="primary" size="small" :icon="Check" @click="handleApprove">审批</el-button>
          <el-button v-if="detail.status !== 'Canceled'" type="danger" size="small" :icon="Delete" @click="handleDelete">删除</el-button>
        </template>
        <template v-else>
          <el-button size="small" @click="handleCancel">取消</el-button>
        </template>
      </div>
    </div>

    <!-- 关键信息条 -->
    <div class="key-info-bar" v-if="mode === 'readonly'">
      <div class="key-item">
        <span class="key-label">交易编号</span>
        <span class="key-value mono">{{ detail.dealNumber || '-' }}</span>
      </div>
      <div class="key-item">
        <el-tag :type="getProductTypeTag(detail.productType)" effect="dark">{{ detail.productType || '-' }}</el-tag>
      </div>
      <div class="key-item">
        <el-tag :type="getStatusType(detail.status)" effect="dark">{{ getStatusLabel(detail.status) }}</el-tag>
      </div>
      <div class="key-item highlight">
        <span class="key-label">总金额</span>
        <span class="key-value mono lg">{{ formatAmount(detail.sellAmount) }} {{ detail.sellCurrency || '' }}</span>
      </div>
      <div class="key-item">
        <span class="key-label">到期日</span>
        <span class="key-value">{{ detail.maturityDate || detail.valueDate || '-' }}</span>
      </div>
    </div>

    <el-card class="detail-card" v-loading="loadingDetail" :body-style="{ padding: '12px 16px' }">
      <!-- 错误提示 -->
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="true"
        @close="errorMessage = ''"
        style="margin-bottom: 8px"
      />

      <section v-if="mode === 'readonly'" class="approval-rule-card" v-loading="approvalRuleLoading">
        <h3 class="section-title-sm">📜 审批规则</h3>
        <el-alert
          v-if="approvalRuleInfo && !approvalRuleInfo.matched"
          type="warning"
          :closable="false"
          title="未命中新交易审批规则，后端将按降级策略处理"
          style="margin-bottom: 8px"
        />
        <el-descriptions :column="5" :size="'small'" border class="summary-grid">
          <el-descriptions-item label="规则编号">{{ approvalRuleInfo?.matchedRule?.ruleNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批层级">
            <el-tag :type="getApprovalLevelTag(approvalRuleInfo?.approvalLevel)" size="small">{{ getApprovalLevelLabel(approvalRuleInfo?.approvalLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="L1 角色">{{ formatRoles(approvalRuleInfo?.level1Roles) }}</el-descriptions-item>
          <el-descriptions-item label="L2 角色">{{ formatRoles(approvalRuleInfo?.level2Roles) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(approvalRuleInfo?.matchedRule?.createdAt || approvalRuleInfo?.matchedRule?.createdTime) }}</el-descriptions-item>
        </el-descriptions>
      </section>

      <!-- 主信息区: readonly 摘要 -->
      <section v-if="mode === 'readonly'" class="main-info-area">
        <!-- 左列: 交易要素 -->
        <div class="info-col">
          <h3 class="section-title-sm">交易要素</h3>
          <el-descriptions :column="2" :size="'small'" border class="summary-grid">
            <el-descriptions-item label="管理主体">{{ entityNames.managementEntity || (detail.managementEntityId ? 'ID:' + detail.managementEntityId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="交易对手">{{ entityNames.counterparty || (detail.counterpartyId ? 'ID:' + detail.counterpartyId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="交易员">{{ entityNames.trader || (detail.traderId ? 'ID:' + detail.traderId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="金融工具">{{ entityNames.instrument || (detail.instrumentId ? 'ID:' + detail.instrumentId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="币种对">{{ entityNames.currencyPair || (detail.currencyPairId ? 'ID:' + detail.currencyPairId : '-') }}</el-descriptions-item>
            <el-descriptions-item label="操作人">{{ detail.operator || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.fixingSource" label="Fixing 来源">{{ detail.fixingSource }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.fixingSource" label="名义本金">
              <span class="mono">{{ formatAmount(detail.notional) }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.fixingSource" label="Fixing 汇率">
              <span v-if="detail.fixingRate" class="mono">{{ Number(detail.fixingRate).toFixed(8) }}</span>
              <span v-else style="color: #909399;">待 RATE_FIX</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.fixingSource && detail.settlementAmount != null" label="结算金额" :span="2">
              <span :class="detail.settlementAmount >= 0 ? 'positive' : 'negative'">{{ formatAmount(detail.settlementAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 右列: 金额 & 日期 -->
        <div class="info-col">
          <h3 class="section-title-sm">金额 & 日期</h3>
          <div class="amount-block">
            <div class="amount-row">
              <span class="amount-label">卖出</span>
              <span class="amount-value mono">{{ formatAmount(detail.sellAmount) }} {{ detail.sellCurrency }}</span>
            </div>
            <div class="amount-row">
              <span class="amount-label">买入</span>
              <span class="amount-value mono">{{ formatAmount(detail.buyAmount) }} {{ detail.buyCurrency }}</span>
            </div>
          </div>
          <el-descriptions :column="2" :size="'small'" border class="summary-grid" style="margin-top: 8px;">
            <el-descriptions-item label="成交汇率"><span class="mono">{{ detail.exchangeRate ? Number(detail.exchangeRate).toFixed(8) : '-' }}</span></el-descriptions-item>
            <el-descriptions-item label="市场汇率"><span class="mono">{{ detail.marketRate ? Number(detail.marketRate).toFixed(8) : '-' }}</span></el-descriptions-item>
            <el-descriptions-item label="点差(bp)"><span class="mono">{{ detail.spreadBp != null ? Number(detail.spreadBp).toFixed(4) : '-' }}</span></el-descriptions-item>
            <el-descriptions-item label="期限(天)">{{ detail.termDays ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="交易日">{{ detail.tradeDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交割日">{{ detail.valueDate || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </section>

      <!-- 主信息区: edit/new/copy 表单 -->
      <section v-else class="edit-form-area">
        <h3 class="section-title-sm">交易要素</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="edit-form">
          <el-row :gutter="12">
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="管理主体" prop="managementEntityId">
                <BaseDataPicker v-model="form.managementEntityId" entity="management-entity" placeholder="管理主体" size="small" :preload-row="preloadRows.managementEntity" @change="onManagementEntityChange" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="交易对手" prop="counterpartyId">
                <BaseDataPicker v-model="form.counterpartyId" entity="counterparty" placeholder="交易对手" size="small" :preload-row="preloadRows.counterparty" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="交易员" prop="traderId">
                <BaseDataPicker v-model="form.traderId" entity="trader" placeholder="交易员" size="small" :preload-row="preloadRows.trader" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="金融工具" prop="instrumentId">
                <BaseDataPicker v-model="form.instrumentId" entity="instrument" placeholder="金融工具" size="small" :preload-row="preloadRows.instrument" @change="onInstrumentChange" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="币种对" prop="currencyPairId">
                <BaseDataPicker v-model="form.currencyPairId" entity="currency-pair" placeholder="币种对" size="small" :preload-row="preloadRows.currencyPair" @change="onCurrencyPairChange" />
              </el-form-item>
            </el-col>
            <!-- 2026-07-05 修复 #3: 买卖币种分开显示 (默认根据货币对联动) -->
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="卖出币种" prop="sellCurrency">
                <el-input v-model="form.sellCurrency" placeholder="由币种对自动联动" size="small" readonly />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="买入币种" prop="buyCurrency">
                <el-input v-model="form.buyCurrency" placeholder="由币种对自动联动" size="small" readonly />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="卖出金额" prop="sellAmount">
                <el-input-number v-model="form.sellAmount" :min="0" :precision="2" :controls="false" size="small" style="width: 100%;" @change="triggerCalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="买入金额" prop="buyAmount">
                <el-input-number v-model="form.buyAmount" :min="0" :precision="2" :controls="false" size="small" style="width: 100%;" @change="triggerCalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="成交汇率" prop="exchangeRate">
                <el-input-number v-model="form.exchangeRate" :min="0" :precision="8" :controls="false" size="small" style="width: 100%;" @change="triggerCalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="市场汇率" prop="marketRate">
                <el-input-number v-model="form.marketRate" :min="0" :precision="8" :controls="false" size="small" style="width: 100%;" @change="triggerCalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="点差(bp)" prop="spreadBp">
                <el-input-number v-model="form.spreadBp" :precision="4" :controls="false" size="small" style="width: 100%;" @change="triggerCalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="交易日" prop="tradeDate">
                <el-date-picker v-model="form.tradeDate" type="date" value-format="YYYY-MM-DD" size="small" style="width: 100%;" @change="triggerCalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="交割日" prop="valueDate">
                <el-date-picker v-model="form.valueDate" type="date" value-format="YYYY-MM-DD" size="small" style="width: 100%;" @change="triggerCalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="操作人" prop="operator">
                <el-input v-model="form.operator" placeholder="操作人" size="small" />
              </el-form-item>
            </el-col>
            <template v-if="isNdf">
              <el-col :xs="24" :sm="12" :md="6">
                <el-form-item label="Fixing 来源" prop="fixingSource">
                  <el-select v-model="form.fixingSource" placeholder="fixing 来源" size="small" clearable style="width: 100%;">
                    <el-option label="BLOOMBERG BFIX" value="BLOOMBERG BFIX" />
                    <el-option label="Reuters FX" value="Reuters FX" />
                    <el-option label="CFETS" value="中国外汇交易中心 CFETS" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-form-item label="名义本金">
                  <el-input-number v-model="form.notional" :min="0" :precision="2" :controls="false" size="small" disabled style="width: 100%;" />
                </el-form-item>
              </el-col>
            </template>
            <el-col :xs="24">
              <el-form-item label="描述">
                <el-input v-model="form.description" type="textarea" :rows="1" maxlength="500" show-word-limit />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </section>

      <!-- ====== Tabs: readonly 模式才显示 ====== -->
      <section v-if="mode === 'readonly'" class="section-tabs">
        <el-tabs v-model="activeTab" class="compact-tabs">
          <el-tab-pane label="审计信息" name="basic">
            <el-descriptions :column="3" :size="'small'" border>
              <el-descriptions-item label="最新 Action">{{ detail.latestActionNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建人">{{ detail.createdBy || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ detail.createdAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新人">{{ detail.updatedBy || '-' }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ detail.updatedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane :label="`DealMap (${dealMapList.length})`" name="dealmap">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('dealmap')">展开全部</el-button>
            </div>
            <el-table :data="dealMapList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="dealmapNumber" label="DealMap 编号" width="160" />
              <el-table-column label="类型" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getDealmapTypeTag(row.dealmapType)" size="small">{{ row.dealmapType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="amount_or_rate" align="right" width="140">
                <template #default="{ row }">
                  <span v-if="row.dealmapType === 'FX_RATE' || row.dealmapType === 'FX_FIX'" class="mono">{{ row.amountOrRate ? Number(row.amountOrRate).toFixed(8) : '-' }}</span>
                  <span v-else class="mono">{{ formatAmount(row.amountOrRate) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="方向" width="80" align="center">
                <el-tag size="small">{{ row.direction }}</el-tag>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
            </el-table>
            <el-empty v-if="dealMapList.length === 0" description="暂无 DealMap 数据" :image-size="60" />
          </el-tab-pane>

          <el-tab-pane :label="`Cashflow (${cashflowList.length})`" name="cashflow">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('cashflow')">展开全部</el-button>
            </div>
            <el-table :data="cashflowList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="cflowNumber" label="现金流编号" width="160" />
              <el-table-column prop="dealmapNumber" label="触发 DealMap" width="160" />
              <el-table-column label="方向" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.direction === 'Inflow' ? 'success' : 'danger'" size="small">{{ row.direction }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="金额" align="right" width="140">
                <template #default="{ row }">
                  <span class="mono">{{ formatAmount(row.amount) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="currency" label="币种" width="70" align="center" />
              <el-table-column prop="valueDate" label="起息日" width="100" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'Created' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="cashflowList.length === 0" description="暂无 Cashflow 数据" :image-size="60" />
          </el-tab-pane>

          <el-tab-pane :label="`Action (${actionList.length})`" name="action">
            <div class="tab-toolbar">
              <el-button type="primary" size="small" link @click="openFullDialog('action')">展开全部</el-button>
            </div>
            <el-table :data="actionList.slice(0, 5)" stripe size="small" class="compact-table" max-height="280">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="actionNumber" label="Action 编号" width="160" />
              <el-table-column label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getActionTypeTag(row.actionType)" size="small">{{ getActionTypeLabel(row.actionType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="operator" label="操作人" width="90" />
              <el-table-column prop="operateAt" label="操作时间" width="160" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.actionStatus === 'Approved' ? 'success' : 'warning'" size="small">{{ row.actionStatus }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="actionList.length === 0" description="暂无 Action 数据" :image-size="60" />
          </el-tab-pane>
        </el-tabs>
      </section>
    </el-card>

    <!-- 底部固定操作条 (仅 edit/new/copy 时显示) -->
    <transition name="fade-up">
      <div v-if="mode !== 'readonly'" class="action-bar bottom">
        <div class="left">
          <el-tag v-if="mode === 'edit'" type="primary" effect="dark" size="small"><el-icon><Edit /></el-icon> 编辑中</el-tag>
          <el-tag v-else-if="mode === 'new'" type="success" effect="dark" size="small"><el-icon><Plus /></el-icon> 新建</el-tag>
          <el-tag v-else-if="mode === 'copy'" type="warning" effect="dark" size="small"><el-icon><DocumentCopy /></el-icon> 复制自 {{ route.query.copyFrom }}</el-tag>
        </div>
        <div class="right">
          <el-button size="small" @click="handleCancel">取消</el-button>
          <el-button type="primary" size="small" :loading="saving" :icon="Check" @click="handleSave">保存</el-button>
        </div>
      </div>
    </transition>

    <!-- RATE_FIX 对话框 (Phase 1: fixDate/fixCurrency/fixMarketRate/fixRemark) -->
    <el-dialog v-model="rateFixVisible" title="NDF RATE_FIX" width="480px" destroy-on-close>
      <el-form :model="rateFixForm" label-width="110px">
        <el-form-item label="Fixing 汇率">
          <el-input-number v-model="rateFixForm.fixingRate" :min="0" :precision="8" :controls="false" style="width: 100%;" placeholder="例: 7.1500" />
        </el-form-item>
        <el-form-item label="Fixing 日期">
          <el-date-picker v-model="rateFixForm.fixDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" placeholder="默认=交割日" />
        </el-form-item>
        <el-form-item label="Fixing 币种">
          <el-select v-model="rateFixForm.fixCurrency" style="width: 100%;" placeholder="默认=买入币种">
            <el-option :label="detail.buyCurrency" :value="detail.buyCurrency" />
            <el-option :label="detail.sellCurrency" :value="detail.sellCurrency" />
          </el-select>
        </el-form-item>
        <el-form-item label="市场参考汇率">
          <el-input-number v-model="rateFixForm.fixMarketRate" :min="0" :precision="8" :controls="false" style="width: 100%;" placeholder="可选,参考值" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="rateFixForm.operator" placeholder="操作人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rateFixForm.fixRemark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateFixVisible = false">取消</el-button>
        <el-button type="primary" :loading="rateFixing" @click="doRateFix">确认 RATE_FIX</el-button>
      </template>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approvalVisible" title="FX 交易审批" width="780px" destroy-on-close>
      <el-alert :title="`交易编号: ${detail.dealNumber || ''}`" type="info" :closable="false" style="margin-bottom: 12px;" />
      <el-table :data="pendingActions" v-loading="approvalLoading" stripe max-height="300">
        <el-table-column prop="actionNumber" label="Action 编号" width="170" />
        <el-table-column prop="actionType" label="类型" width="100" align="center">
          <template #default="{ row }"><el-tag size="small">{{ row.actionType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="actionStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.actionStatus === 'Approved' ? 'success' : row.actionStatus === 'Rejected' ? 'danger' : 'warning'" size="small">
              {{ row.actionStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="doApprove(row)">通过</el-button>
            <el-button type="danger" link size="small" @click="doReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!approvalLoading && pendingActions.length === 0" description="该交易没有可审批的 Action" />
      <template #footer>
        <el-button @click="approvalVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 展开全部 Dialog -->
    <el-dialog v-model="fullDialogVisible" :title="fullDialogTitle" width="80%" top="5vh" destroy-on-close>
      <el-table :data="fullDialogData" stripe size="small" max-height="70vh">
        <el-table-column v-for="col in fullDialogColumns" :key="col.prop" :prop="col.prop" :label="col.label" :width="col.width" :align="col.align || 'left'" :show-overflow-tooltip="true">
          <template v-if="col.tag" #default="{ row }">
            <el-tag :type="col.tag(row)" size="small">{{ col.formatter ? col.formatter(row) : row[col.prop] }}</el-tag>
          </template>
          <template v-else-if="col.formatter" #default="{ row }">
            <span class="mono">{{ col.formatter(row) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, CopyDocument, Delete, DocumentCopy, Edit, MagicStick, Plus } from '@element-plus/icons-vue'
import {
  getFxDeal, rateFixFxDeal, calculateFxDeal, createFxDeal, updateFxDeal,
  deleteFxDeal, copyFxDeal, approveFxAction, rejectFxAction
} from '@/api/dealing/fxDeal'
import { listActionsByDeal } from '@/api/dealing/acDeal'
import { matchDealApprovalRule } from '@/api/basedata/dealApprovalRule'
import { getManagementEntity } from '@/api/basedata/managementEntity'
import { getCounterparty } from '@/api/basedata/counterparty'
import { getTrader } from '@/api/basedata/trader'
import { getInstrument } from '@/api/basedata/instrument'
import { getCurrencyPair } from '@/api/basedata/currencyPair'
import BaseDataPicker from '@/components/picker/BaseDataPicker.vue'
import ModeBadge from '@/components/common/ModeBadge.vue'

const route = useRoute()
const router = useRouter()

// ====== 模式: new | readonly | edit | copy ======
const mode = computed(() => {
  if (route.query.new) return 'new'
  if (route.query.edit) return 'edit'
  if (route.query.copyFrom) return 'copy'
  return 'readonly'
})

const detail = ref({})
const dealMapList = ref([])
const cashflowList = ref([])
const actionList = ref([])
const approvalRuleInfo = ref(null)
const approvalRuleLoading = ref(false)
const activeTab = ref('basic')
const loadingDetail = ref(false)
const errorMessage = ref('')

const entityNames = reactive({
  managementEntity: '',
  counterparty: '',
  trader: '',
  instrument: '',
  currencyPair: ''
})

// RATE_FIX
const rateFixVisible = ref(false)
const rateFixing = ref(false)
const rateFixForm = reactive({
  fixingRate: null,
  fixDate: '',
  fixCurrency: '',
  fixMarketRate: null,
  operator: 'admin',
  fixRemark: ''
})

// 审批
const approvalVisible = ref(false)
const approvalLoading = ref(false)
const pendingActions = ref([])

// 展开全部 Dialog
const fullDialogVisible = ref(false)
const fullDialogTitle = ref('')
const fullDialogData = ref([])
const fullDialogColumns = ref([])

// ====== 表单(用于 new/edit/copy) ======
const formRef = ref()
const saving = ref(false)
let calculateTimer = null

const emptyForm = () => ({
  dealNumber: '',
  managementEntityId: null,
  managementEntityName: '',
  counterpartyId: null,
  counterpartyName: '',
  traderId: null,
  traderName: '',
  instrumentId: null,
  instrumentName: '',
  currencyPairId: null,
  currencyPairName: '',
  sellCurrency: '',
  sellAmount: null,
  buyCurrency: '',
  buyAmount: null,
  exchangeRate: null,
  marketRate: null,
  spreadBp: null,
  tradeDate: new Date().toISOString().slice(0, 10),
  valueDate: new Date().toISOString().slice(0, 10),
  termDays: '',
  maturityDate: '',
  notional: null,
  fixingSource: '',
  description: '',
  remark: '',
  operator: 'admin'
})

const form = reactive(emptyForm())

const isNdf = computed(() => !!form.fixingSource && form.fixingSource.length > 0)

const rules = {
  managementEntityId: [{ required: true, message: '管理主体不能为空', trigger: 'change' }],
  counterpartyId: [{ required: true, message: '交易对手不能为空', trigger: 'change' }],
  traderId: [{ required: true, message: '交易员不能为空', trigger: 'change' }],
  instrumentId: [{ required: true, message: '金融工具不能为空', trigger: 'change' }],
  currencyPairId: [{ required: true, message: '币种对不能为空', trigger: 'change' }],
  sellAmount: [{ required: true, message: '卖出金额不能为空', trigger: 'change' }],
  buyAmount: [{ required: true, message: '买入金额不能为空', trigger: 'change' }],
  exchangeRate: [{ required: true, message: '成交汇率不能为空', trigger: 'change' }],
  marketRate: [{ required: true, message: '市场汇率不能为空', trigger: 'change' }],
  spreadBp: [{ required: true, message: '点差不能为空', trigger: 'change' }],
  tradeDate: [{ required: true, message: '交易日不能为空', trigger: 'change' }],
  valueDate: [{ required: true, message: '交割日不能为空', trigger: 'change' }],
  operator: [{ required: true, message: '操作人不能为空', trigger: 'blur' }]
}

const fillFormFromObject = (src) => {
  Object.assign(form, {
    dealNumber: src.dealNumber || '',
    managementEntityId: src.managementEntityId ?? null,
    managementEntityName: src.managementEntityName || '',
    counterpartyId: src.counterpartyId ?? null,
    counterpartyName: src.counterpartyName || '',
    traderId: src.traderId ?? null,
    traderName: src.traderName || '',
    instrumentId: src.instrumentId ?? null,
    instrumentName: src.instrumentName || '',
    currencyPairId: src.currencyPairId ?? null,
    currencyPairName: src.currencyPairName || '',
    sellCurrency: src.sellCurrency || '',
    sellAmount: src.sellAmount ?? null,
    buyCurrency: src.buyCurrency || '',
    buyAmount: src.buyAmount ?? null,
    exchangeRate: src.exchangeRate ?? null,
    marketRate: src.marketRate ?? null,
    spreadBp: src.spreadBp ?? null,
    tradeDate: src.tradeDate || new Date().toISOString().slice(0, 10),
    valueDate: src.valueDate || new Date().toISOString().slice(0, 10),
    termDays: src.termDays || '',
    maturityDate: src.maturityDate || '',
    notional: src.notional ?? null,
    fixingSource: src.fixingSource || '',
    description: src.description || '',
    remark: src.remark || '',
    operator: src.operator || 'admin'
  })
}

/**
 * 2026-07-05 修复 #2: FX 复制后 BaseDataPicker 显示 ID
 * <p>原因: Picker v-model 接收 ID，没有 preloadRow 时只显示 ID 数字。</p>
 * <p>修复: 注入 preloadRows - 把后端返回的关联实体名称预填到 Picker,
 * mount 时直接展示 "code (name)" 形式, 也修复 #3 buy/sell 币种对联动。</p>
 */
const preloadRows = reactive({
  managementEntity: null,
  counterparty: null,
  trader: null,
  instrument: null,
  currencyPair: null
})

function splitCodeName(nameStr) {
  if (!nameStr) return { code: null, name: null }
  if (nameStr.includes('(')) {
    return {
      code: nameStr.substring(0, nameStr.indexOf('(')).trim(),
      name: nameStr.substring(nameStr.indexOf('(') + 1, nameStr.lastIndexOf(')')).trim()
    }
  }
  return { code: null, name: nameStr }
}

function applyPreloadFromCopyData(src) {
  if (src.managementEntityId && src.managementEntityName) {
    const { code, name } = splitCodeName(src.managementEntityName)
    preloadRows.managementEntity = { id: src.managementEntityId, code, name }
  }
  if (src.counterpartyId && src.counterpartyName) {
    const { code, name } = splitCodeName(src.counterpartyName)
    preloadRows.counterparty = { id: src.counterpartyId, code, name }
  }
  if (src.traderId && src.traderName) {
    const { code, name } = splitCodeName(src.traderName)
    preloadRows.trader = { id: src.traderId, code, name }
  }
  if (src.instrumentId && src.instrumentName) {
    if (src.instrumentName.includes('(')) {
      preloadRows.instrument = {
        id: src.instrumentId,
        instrumentCode: src.instrumentName.substring(0, src.instrumentName.indexOf('(')).trim(),
        instrumentName: src.instrumentName.substring(src.instrumentName.indexOf('(') + 1, src.instrumentName.lastIndexOf(')')).trim()
      }
    } else {
      preloadRows.instrument = { id: src.instrumentId, instrumentCode: src.instrumentName, instrumentName: '' }
    }
  }
  if (src.currencyPairId && src.currencyPairName) {
    // 格式 "EURUSD EUR/USD" - pairCode + 币种对
    const parts = src.currencyPairName.split(' ')
    preloadRows.currencyPair = {
      id: src.currencyPairId,
      pairCode: parts[0],
      currency1: form.sellCurrency,
      currency2: form.buyCurrency
    }
  }
}

const isDirty = computed(() => {
  if (mode.value === 'new' || mode.value === 'copy') return true
  return JSON.stringify(form) !== JSON.stringify(detail.value)
})

const isNdfForDetail = computed(() => {
  return detail.value.fixingSource && detail.value.fixingSource.length > 0
})

const getStatusLabel = (s) => ({ New: '新建', Active: '活跃', Canceled: '已删除' }[s] || s)
const getStatusType = (s) => ({ New: 'info', Active: 'success', Canceled: 'danger' }[s] || 'info')
const getProductTypeTag = (t) => ({ SPOT: 'success', FWD: 'warning', NDF: 'info' }[t] || 'info')
const getActionTypeLabel = (t) => ({ DEAL: '创建', UPDATE: '修改', DELETE: '删除', RATE_FIX: 'Fixing' }[t] || t)
const getActionTypeTag = (t) => ({ DEAL: 'success', UPDATE: 'warning', DELETE: 'danger', RATE_FIX: 'info' }[t] || 'info')
const getApprovalLevelLabel = (level) => ({ LEVEL_0: '无需审批', LEVEL_1: '一层审批', LEVEL_2: '二层审批' }[level] || level || '-')
const getApprovalLevelTag = (level) => ({ LEVEL_0: 'success', LEVEL_1: 'warning', LEVEL_2: 'danger' }[level] || 'info')
const formatRoles = (roles) => Array.isArray(roles) && roles.length ? roles.join(', ') : '-'
const formatDateTime = (val) => {
  if (!val) return '-'
  if (typeof val === 'string') return val.replace('T', ' ').substring(0, 19)
  return String(val)
}
const toNumberParam = (value) => {
  if (value === null || value === undefined || value === '') return undefined
  const n = Number(value)
  return Number.isFinite(n) ? n : undefined
}
const getDealmapTypeTag = (t) => {
  if (!t) return 'info'
  if (t.startsWith('FX_BUY')) return 'success'
  if (t.startsWith('FX_SELL')) return 'warning'
  if (t === 'FX_RATE') return 'info'
  if (t === 'FX_FIX') return 'danger'
  return 'info'
}
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '-'
  return new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(amount)
}
const getCflowForDealmap = (dealmapNumber) => cashflowList.value.find(c => c.dealmapNumber === dealmapNumber)

const onCurrencyPairChange = (row) => {
  if (!row) return
  form.sellCurrency = row.baseCurrency || row.currency1 || ''
  form.buyCurrency = row.quoteCurrency || row.currency2 || ''
  triggerCalculate()
}

const onManagementEntityChange = (row) => {
  if (row && row.id) form.managementEntityId = row.id
}

const onInstrumentChange = (row) => {
  if (!row) return
  const type = (row.instrumentType || '').toUpperCase()
  if (type.includes('NDF')) {
    if (!form.fixingSource) form.fixingSource = 'BLOOMBERG BFIX'
  } else {
    form.fixingSource = ''
    form.notional = null
  }
}

watch(() => form.sellAmount, (val) => {
  if (isNdf.value && val) form.notional = val
})

const triggerCalculate = () => {
  if (calculateTimer) clearTimeout(calculateTimer)
  calculateTimer = setTimeout(doCalculate, 300)
}

const doCalculate = async () => {
  const req = {
    sellAmount: form.sellAmount,
    buyAmount: form.buyAmount,
    exchangeRate: form.exchangeRate,
    marketRate: form.marketRate,
    spreadBp: form.spreadBp,
    tradeDate: form.tradeDate,
    valueDate: form.valueDate
  }
  let filled = 0
  if (req.sellAmount != null) filled++
  if (req.buyAmount != null) filled++
  if (req.exchangeRate != null) filled++
  if (req.marketRate != null) filled++
  if (req.spreadBp != null) filled++
  if (filled < 2) return
  try {
    const res = await calculateFxDeal(req)
    const d = res.data
    if (d.buyAmount != null) form.buyAmount = d.buyAmount
    if (d.sellAmount != null) form.sellAmount = d.sellAmount
    if (d.exchangeRate != null) form.exchangeRate = d.exchangeRate
    if (d.marketRate != null) form.marketRate = d.marketRate
    if (d.spreadBp != null) form.spreadBp = d.spreadBp
    if (d.termDays != null) form.termDays = d.termDays
    if (d.maturityDate != null) form.maturityDate = d.maturityDate
  } catch (e) {
    console.warn('Calculate failed:', e?.message || e)
  }
}

const handleBack = () => router.push('/dealing/fx-deal')

const enterEdit = () => {
  fillFormFromObject(detail.value)
  router.replace({ path: '/dealing/fx-deal/detail', query: { dealNumber: detail.value.dealNumber, edit: 1 } })
}

const enterCopy = async () => {
  try {
    const res = await copyFxDeal(detail.value.dealNumber)
    const data = res.data || res
    fillFormFromObject(data)
    applyPreloadFromCopyData(data)
    form.dealNumber = ''
    router.replace({ path: '/dealing/fx-deal/detail', query: { copyFrom: detail.value.dealNumber } })
  } catch (e) {
    ElMessage.error(e?.message || '复制失败')
  }
}

const handleCancel = async () => {
  if ((mode.value === 'edit' || mode.value === 'copy') && isDirty.value) {
    try {
      await ElMessageBox.confirm('放弃当前修改?', '确认取消', { type: 'warning', confirmButtonText: '放弃修改', cancelButtonText: '继续编辑' })
    } catch (e) {
      return
    }
  }
  if (mode.value === 'copy' || mode.value === 'edit') {
    if (detail.value.dealNumber) {
      router.replace({ path: '/dealing/fx-deal/detail', query: { dealNumber: detail.value.dealNumber } })
    } else {
      handleBack()
    }
  } else {
    handleBack()
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    errorMessage.value = '请检查表单填写'
    setTimeout(() => { errorMessage.value = '' }, 5000)
    return
  }
  saving.value = true
  try {
    let res
    if (mode.value === 'edit' && form.dealNumber) {
      res = await updateFxDeal(form)
      ElMessage.success('保存成功')
      await loadData(form.dealNumber)
      router.replace({ path: '/dealing/fx-deal/detail', query: { dealNumber: form.dealNumber } })
    } else {
      const createData = { ...form }
      delete createData.dealNumber
      res = await createFxDeal(createData)
      const newNumber = res.data?.dealNumber
      ElMessage.success('创建成功')
      if (newNumber) {
        router.replace({ path: '/dealing/fx-deal/detail', query: { dealNumber: newNumber } })
      } else {
        handleBack()
      }
    }
  } catch (e) {
    errorMessage.value = e?.message || '保存失败'
    setTimeout(() => { errorMessage.value = '' }, 5000)
  } finally {
    saving.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除 FX 交易 ${detail.value.dealNumber}？将级联软删 Deal/DealMap/Cashflow。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteFxDeal(detail.value.id)
    ElMessage.success('删除成功')
    handleBack()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleApprove = async () => {
  approvalVisible.value = true
  approvalLoading.value = true
  try {
    const res = await listActionsByDeal(detail.value.dealNumber)
    const records = res?.data?.records || res?.data || res || []
    pendingActions.value = (Array.isArray(records) ? records : []).filter(a => a.actionStatus === 'Pending' || a.actionStatus === 'Approved')
  } catch (e) {
    ElMessage.error('加载 Action 失败')
  } finally {
    approvalLoading.value = false
  }
}

const doApprove = async (action) => {
  try {
    await approveFxAction(action.actionNumber, { approver: 'admin', remark: 'FX 审批通过' })
    ElMessage.success(`Action ${action.actionNumber} 审批通过`)
    await handleApprove()
    await loadData(detail.value.dealNumber)
  } catch (e) {
    ElMessage.error(e?.message || '审批失败')
  }
}

const doReject = async (action) => {
  try {
    await ElMessageBox.confirm(`确认驳回 Action ${action.actionNumber}？`, '驳回确认', { type: 'warning' })
    await rejectFxAction(action.actionNumber, { approver: 'admin', remark: 'FX 审批驳回' })
    ElMessage.success('已驳回')
    await handleApprove()
    await loadData(detail.value.dealNumber)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '驳回失败')
  }
}

const openRateFixDialog = () => {
  rateFixForm.fixingRate = null
  rateFixForm.fixDate = detail.value.valueDate || ''
  rateFixForm.fixCurrency = detail.value.buyCurrency || ''
  rateFixForm.fixMarketRate = null
  rateFixForm.operator = 'admin'
  rateFixForm.fixRemark = ''
  rateFixVisible.value = true
}

const doRateFix = async () => {
  if (!rateFixForm.fixingRate) {
    ElMessage.error('请输入 Fixing 汇率')
    return
  }
  rateFixing.value = true
  try {
    const payload = {
      fixingRate: rateFixForm.fixingRate,
      fixDate: rateFixForm.fixDate || null,
      fixCurrency: rateFixForm.fixCurrency || detail.value.buyCurrency,
      fixMarketRate: rateFixForm.fixMarketRate || null,
      operator: rateFixForm.operator || 'admin',
      fixRemark: rateFixForm.fixRemark || null
    }
    const res = await rateFixFxDeal(detail.value.id, payload)
    const data = res.data || res
    rateFixVisible.value = false
    const dirLabel = data.direction === 'Inflow' ? '流入' : '流出'
    ElMessage.success(`RATE_FIX 完成 | 差额: ${Number(data.settlementAmount).toFixed(2)} ${data.currency} (${dirLabel}) | Cashflow: ${data.dealmapNumber}`)
    // 自动跳到 Cashflow Tab
    activeTab.value = 'cashflow'
    await loadData(detail.value.dealNumber)
  } catch (e) {
    ElMessage.error(e?.message || 'RATE_FIX 失败')
  } finally {
    rateFixing.value = false
  }
}

// 展开全部 Dialog
const openFullDialog = (tabName) => {
  if (tabName === 'dealmap') {
    fullDialogTitle.value = `DealMap (${dealMapList.value.length})`
    fullDialogData.value = dealMapList.value
    fullDialogColumns.value = [
      { prop: 'dealmapNumber', label: 'DealMap 编号', width: 160 },
      { prop: 'actionNumber', label: 'Action 编号', width: 160 },
      { prop: 'dealmapType', label: '类型', width: 120, tag: getDealmapTypeTag },
      { prop: 'amountOrRate', label: '金额/汇率', width: 140, align: 'right', formatter: (r) => formatAmount(r.amountOrRate) },
      { prop: 'direction', label: '方向', width: 80, align: 'center' },
      { prop: 'description', label: '描述', width: 200 }
    ]
  } else if (tabName === 'cashflow') {
    fullDialogTitle.value = `Cashflow (${cashflowList.value.length})`
    fullDialogData.value = cashflowList.value
    fullDialogColumns.value = [
      { prop: 'cflowNumber', label: '现金流编号', width: 160 },
      { prop: 'dealmapNumber', label: '触发 DealMap', width: 160 },
      { prop: 'direction', label: '方向', width: 80, align: 'center', tag: (r) => r.direction === 'Inflow' ? 'success' : 'danger' },
      { prop: 'amount', label: '金额', width: 140, align: 'right', formatter: (r) => formatAmount(r.amount) },
      { prop: 'currency', label: '币种', width: 80, align: 'center' },
      { prop: 'valueDate', label: '起息日', width: 110, align: 'center' },
      { prop: 'status', label: '状态', width: 100, align: 'center', tag: (r) => r.status === 'Created' ? 'success' : 'info' }
    ]
  } else if (tabName === 'action') {
    fullDialogTitle.value = `Action (${actionList.value.length})`
    fullDialogData.value = actionList.value
    fullDialogColumns.value = [
      { prop: 'actionNumber', label: 'Action 编号', width: 170 },
      { prop: 'actionType', label: '类型', width: 100, align: 'center', tag: getActionTypeTag, formatter: getActionTypeLabel },
      { prop: 'operator', label: '操作人', width: 100 },
      { prop: 'operateAt', label: '操作时间', width: 180 },
      { prop: 'actionStatus', label: '状态', width: 100, align: 'center', tag: (r) => r.actionStatus === 'Approved' ? 'success' : 'warning' }
    ]
  }
  fullDialogVisible.value = true
}

const loadApprovalRule = async (d) => {
  approvalRuleInfo.value = null
  if (!d) return
  approvalRuleLoading.value = true
  try {
    const params = {
      managementEntityId: toNumberParam(d.managementEntityId),
      counterpartyId: toNumberParam(d.counterpartyId),
      instrumentId: toNumberParam(d.instrumentId),
      dealerId: toNumberParam(d.dealerId || d.traderId),
      actionType: 'SUBMIT'
    }
    Object.keys(params).forEach(key => params[key] === undefined && delete params[key])
    const res = await matchDealApprovalRule(params)
    approvalRuleInfo.value = res.data || null
  } catch (e) {
    console.warn('[FX] 审批规则匹配失败:', e?.message || e)
    approvalRuleInfo.value = null
  } finally {
    approvalRuleLoading.value = false
  }
}

const loadData = async (dealNumber) => {
  if (!dealNumber) return
  loadingDetail.value = true
  try {
    const res = await getFxDeal(dealNumber)
    const d = res.data
    detail.value = d
    dealMapList.value = d.dealMapList || []
    cashflowList.value = d.cashflowList || []
    actionList.value = d.actionList || []
    await loadApprovalRule(d)
    resolveEntityNames(d)
  } catch (e) {
    ElMessage.error(e?.message || '加载失败:请重试')
  } finally {
    loadingDetail.value = false
  }
}

const resolveEntityNames = async (d) => {
  const fetchers = []
  if (d.managementEntityId) fetchers.push(getManagementEntity(d.managementEntityId).then(r => { const x = r?.data || r; entityNames.managementEntity = x.name || x.code || '' }).catch(() => {}))
  if (d.counterpartyId) fetchers.push(getCounterparty(d.counterpartyId).then(r => { const x = r?.data || r; entityNames.counterparty = x.name || x.code || '' }).catch(() => {}))
  if (d.traderId) fetchers.push(getTrader(d.traderId).then(r => { const x = r?.data || r; entityNames.trader = x.name || x.code || '' }).catch(() => {}))
  if (d.instrumentId) fetchers.push(getInstrument(d.instrumentId).then(r => { const x = r?.data || r; entityNames.instrument = x.instrumentName || x.instrumentCode || '' }).catch(() => {}))
  if (d.currencyPairId) fetchers.push(getCurrencyPair(d.currencyPairId).then(r => { const x = r?.data || r; entityNames.currencyPair = x.pairCode || '' }).catch(() => {}))
  await Promise.allSettled(fetchers)
}

const loadCopyData = async (dealNumber) => {
  try {
    const res = await copyFxDeal(dealNumber)
    const data = res.data || res
    fillFormFromObject(data)
    applyPreloadFromCopyData(data)
    form.dealNumber = ''
  } catch (e) {
    ElMessage.error('复制失败: ' + (e?.message || '请重试'))
  }
}

const init = async () => {
  if (mode.value === 'new') {
    Object.assign(form, emptyForm())
    return
  }
  // copy 模式: 直接通过 copyFrom 拉取可复制字段
  if (mode.value === 'copy') {
    const copyFrom = route.query.copyFrom
    if (copyFrom) await loadCopyData(copyFrom)
    return
  }
  const num = route.query.dealNumber
  if (!num) return
  await loadData(num)
  if (mode.value === 'edit') {
    fillFormFromObject(detail.value)
  }
}

onMounted(init)
</script>

<style scoped>
.fx-deal-detail { padding-bottom: 64px; }

/* === 顶部工具条 === */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  height: 48px;
}
.action-bar .right { display: flex; gap: 6px; align-items: center; }
.action-bar .left { display: flex; gap: 8px; align-items: center; }
.page-title { font-size: 14px; font-weight: 600; color: #303133; }

.action-bar.bottom {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  border-top: 1px solid #ebeef5;
  border-bottom: none;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 99;
  height: 56px;
}

/* === 关键信息条 === */
.key-info-bar {
  display: flex;
  align-items: center;
  height: 80px;
  padding: 0 20px;
  margin: 8px 0;
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%);
  border: 1px solid #d9ecff;
  border-radius: 6px;
  gap: 24px;
}
.key-item { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.key-label { font-size: 12px; color: #909399; }
.key-value { font-size: 14px; color: #303133; font-weight: 500; }
.key-value.mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }
.key-value.lg { font-size: 18px; font-weight: 700; color: #303133; }
.key-item.highlight .key-value { color: #409eff; }

@media (max-width: 1599px) {
  .key-info-bar .key-item:nth-child(5) { display: none; }
}
@media (max-width: 1199px) {
  .key-info-bar .key-item:nth-child(2),
  .key-info-bar .key-item:nth-child(5) { display: none; }
}
@media (max-width: 959px) {
  .key-info-bar { flex-wrap: wrap; height: auto; padding: 8px; }
  .key-info-bar .key-item { flex: 1 1 50%; }
}

/* === 主信息区 === */
.detail-card { margin-top: 4px; }
.approval-rule-card { margin-bottom: 10px; }
.main-info-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  min-height: 480px;
}
.info-col { min-width: 0; }
.section-title-sm {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px 0;
  padding-left: 6px;
  border-left: 3px solid #409eff;
  line-height: 1.2;
}
.summary-grid :deep(.el-descriptions__label) { width: 90px; padding: 4px 8px; font-size: 12px; }
.summary-grid :deep(.el-descriptions__content) { padding: 4px 8px; font-size: 12px; }

@media (max-width: 1199px) {
  .main-info-area { grid-template-columns: 1fr; }
}

.amount-block { background: #fafbfc; border-radius: 4px; padding: 10px 12px; }
.amount-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; }
.amount-row + .amount-row { border-top: 1px dashed #e4e7ed; }
.amount-label { font-size: 12px; color: #606266; }
.amount-value { font-size: 18px; font-weight: 700; color: #303133; font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }

/* === 编辑表单 === */
.edit-form-area { min-height: 480px; }
.edit-form { padding: 0 4px; }
.edit-form :deep(.el-form-item) { margin-bottom: 12px; }
.edit-form :deep(.el-form-item__label) { font-weight: 500; font-size: 12px; line-height: 1.4; padding-bottom: 2px; }

/* === Tabs 区 === */
.section-tabs { margin-top: 8px; }
.compact-tabs :deep(.el-tabs__header) { margin: 0 0 4px 0; }
.compact-tabs :deep(.el-tabs__item) { padding: 0 12px; height: 32px; line-height: 32px; font-size: 13px; }
.compact-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; }
.compact-tabs :deep(.el-tab-pane) { max-height: 360px; overflow: hidden; }
.tab-toolbar { display: flex; justify-content: flex-end; margin-bottom: 4px; }

.compact-table :deep(.el-table__row) { height: 36px; }
.compact-table :deep(.el-table__cell) { padding: 4px 0; font-size: 12px; }

/* === 通用 === */
.mono { font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace; }
.positive { color: #67c23a; font-weight: 600; }
.negative { color: #f56c6c; font-weight: 600; }
.field-hint { font-size: 12px; color: #909399; margin-top: 4px; }

.fade-up-enter-active,
.fade-up-leave-active { transition: all 0.2s ease; }
.fade-up-enter-from,
.fade-up-leave-to { opacity: 0; transform: translateY(10px); }
</style>