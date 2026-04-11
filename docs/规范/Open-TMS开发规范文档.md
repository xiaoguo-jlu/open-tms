# Open-TMS 开发规范文档

**项目**: Open-TMS (企业级资金管理系统)  
**角色**: 技术架构师 (TA)  
**版本**: v1.0  
**日期**: 2026-04-06

---

## 一、数据库设计规范

### 1.1 命名规范

#### 1.1.1 数据库命名

| 规范项 | 规则 | 示例 |
|--------|------|------|
| 数据库名 | `{project}_{env}` | opentms_dev / opentms_prod |
| 表名 | `tms_{module}_{type}` | tms_transaction_t / tms_user_t |
| 主键 | `id` BIGSERIAL PRIMARY KEY | id BIGINT PRIMARY KEY |
| 外键 | `{table}_id` | bank_id / user_id |

#### 1.1.2 表类型后缀

| 后缀 | 含义 | 示例 |
|------|------|------|
| `_t` | 主表/实体表 | tms_user_t |
| `_d` | 字典表/配置表 | tms_dict_d |
| `_log` | 日志表 | tms_audit_log |
| `_rel` | 关联表 | tms_user_role_rel |
| `_his` | 历史表 | tms_transaction_his |
| `_tmp` | 临时表 | tms_calc_tmp |

#### 1.1.3 字段命名

```sql
-- 通用字段（所有表必须包含）
created_by    VARCHAR(50)   NOT NULL   -- 创建人
created_at    TIMESTAMP     NOT NULL   -- 创建时间
updated_by    VARCHAR(50)               -- 最后更新人
updated_at    TIMESTAMP                 -- 更新时间
version       INT           DEFAULT 0  -- 乐观锁版本号
deleted       CHAR(1)       DEFAULT '0' -- 软删除标记

-- 状态字段
status        CHAR(1)       DEFAULT '1'  -- '1'=启用 '0'=禁用

-- 编码字段
xxx_code      VARCHAR(50)   NOT NULL    -- 业务编码（唯一）
xxx_no        VARCHAR(50)   NOT NULL    -- 流水号（唯一）

-- 金额字段（必须使用DECIMAL）
amount        DECIMAL(18,2)             -- 金额（2位小数）
balance       DECIMAL(18,2)             -- 余额
exchange_rate DECIMAL(18,8)             -- 汇率（8位小数）
interest_rate DECIMAL(10,4)             -- 利率（4位小数）

-- 日期字段
value_date    DATE           NOT NULL   -- 起息日
maturity_date DATE           NOT NULL   -- 到期日
```

### 1.2 表设计规范

#### 1.2.1 主键设计

```sql
-- 方式一：自增主键（推荐用于交易表）
id BIGSERIAL PRIMARY KEY

-- 方式二：UUID（用于分布式场景）
id UUID PRIMARY KEY DEFAULT gen_random_uuid()

-- 方式三：业务主键 + 自增ID组合
id BIGSERIAL PRIMARY KEY,
transaction_no VARCHAR(50) NOT NULL UNIQUE
```

#### 1.2.2 索引设计

```sql
-- 业务查询索引（必须创建）
CREATE INDEX idx_{table}_{column} ON {table}(column);

-- 复合索引（按查询频率排序）
CREATE INDEX idx_{table}_{col1}_{col2} ON {table}(col1, col2);

-- 唯一索引（业务编码）
CREATE UNIQUE INDEX uidx_{table}_code ON {table}(code);

-- 部分索引（高频查询条件）
CREATE INDEX idx_{table}_status ON {table}(status) WHERE status = '1';
```

#### 1.2.3 约束设计

```sql
-- 必须包含的约束
NOT NULL           -- 业务关键字段
UNIQUE             -- 业务编码类字段
CHECK              -- 枚举值校验
FOREIGN KEY        -- 外键关联

-- 金额字段CHECK约束
CONSTRAINT chk_amount CHECK (amount > 0)
CONSTRAINT chk_rate CHECK (interest_rate >= 0 AND interest_rate <= 1)
```

### 1.3 审计与幂等

#### 1.3.1 审计字段

```sql
-- 资金交易表必须包含幂等字段
id              BIGSERIAL PRIMARY KEY,
transaction_no  VARCHAR(50) NOT NULL UNIQUE,  -- 幂等键
idempotency_key VARCHAR(64),                    -- 外部幂等key
version         INT DEFAULT 0,                   -- 乐观锁

-- 审计日志（自动记录）
CREATE TABLE tms_audit_log_t (
    id              BIGSERIAL PRIMARY KEY,
    table_name      VARCHAR(50) NOT NULL,
    record_id       BIGINT NOT NULL,
    operation_type  VARCHAR(20) NOT NULL,
    operation_user  VARCHAR(50) NOT NULL,
    operation_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_value    JSONB,
    after_value     JSONB,
    ip_address      VARCHAR(50),
    remark          VARCHAR(500)
);
```

#### 1.3.2 幂等性设计

```sql
-- 幂等表（防重复提交）
CREATE TABLE tms_idempotency_t (
    idempotency_key VARCHAR(64) PRIMARY KEY,
    request_hash    VARCHAR(64),
    response_data   JSONB,
    expire_time     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 交易状态机（防止重复执行）
CREATE TABLE tms_transaction_status_t (
    transaction_no  VARCHAR(50) PRIMARY KEY,
    current_status  VARCHAR(20) NOT NULL,
    lock_version    INT DEFAULT 0,
    updated_at      TIMESTAMP
);
```

### 1.4 表创建模板

```sql
-- ============================================
-- {表描述}
-- 模块: {module}
-- ============================================
CREATE TABLE tms_{table}_t (
    id                  BIGSERIAL PRIMARY KEY,
    {table}_code        VARCHAR(50) NOT NULL UNIQUE,
    {table}_name        VARCHAR(200) NOT NULL,
    status              CHAR(1) NOT NULL DEFAULT '1',
    
    -- 公共审计字段
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,
    deleted             CHAR(1) DEFAULT '0'
);

COMMENT ON TABLE tms_{table}_t IS '{表描述}';
CREATE INDEX idx_{table}_code ON tms_{table}_t({table}_code);
CREATE INDEX idx_{table}_status ON tms_{table}_t(status);
```

---

## 二、后端代码开发规范

### 2.1 代码组织

```
src/
├── main/
│   ├── java/com/opentms/
│   │   ├── common/              # 公共模块
│   │   │   ├── config/         # 配置类
│   │   │   ├── constant/       # 常量定义
│   │   │   ├── exception/      # 异常定义
│   │   │   ├── model/          # 公共模型
│   │   │   └── util/           # 工具类
│   │   ├── {module}/           # 业务模块
│   │   │   ├── controller/     # 接口层
│   │   │   ├── service/        # 服务层
│   │   │   ├── mapper/         # 数据访问层
│   │   │   ├── entity/         # 实体类
│   │   │   ├── dto/            # 数据传输对象
│   │   │   └── vo/             # 视图对象
│   │   └── OpenTmsApplication.java
│   └── resources/
│       ├── mapper/             # MyBatis映射文件
│       ├── application.yml     # 应用配置
│       └── logback.xml         # 日志配置
└── test/
    └── java/                   # 测试代码
```

### 2.2 命名规范

#### 2.2.1 类命名

| 类型 | 规范 | 示例 |
|------|------|------|
| Controller | `{Entity}Controller` | TransactionController |
| Service | `{Entity}Service` | TransactionService |
| ServiceImpl | `{Entity}ServiceImpl` | TransactionServiceImpl |
| Mapper | `{Entity}Mapper` | TransactionMapper |
| Entity | `{Entity}` | Transaction |
| DTO | `{Entity}DTO` | TransactionDTO |
| VO | `{Entity}VO` | TransactionVO |
| Constant | `{Module}Constants` | TransactionConstants |

#### 2.2.2 方法命名

```java
// 查询方法
getById()           // 按ID查询
getByCode()         // 按编码查询
listByCondition()   // 条件查询
listAll()           // 查询所有

// 保存方法
save()              // 新增
saveBatch()         // 批量新增
saveOrUpdate()      // 存在则更新

// 更新方法
updateById()        // 按ID更新
updateByCode()      // 按编码更新

// 删除方法
removeById()        // 按ID删除（物理删除）
removeByIds()       // 批量删除
softDeleteById()    // 软删除

// 业务方法
submit()            // 提交审批
approve()           // 审批通过
reject()            // 审批拒绝
execute()           // 执行交易
cancel()            // 撤销
```

#### 2.2.3 变量命名

```java
// 常量（全部大写，下划线分隔）
public static final String STATUS_DRAFT = "DRAFT";
private static final int MAX_RETRY_TIMES = 3;

// 成员变量（驼峰命名）
private Long id;
private String transactionNo;
private BigDecimal amount;

// 方法参数（意义明确）
public void process(String transactionNo, BigDecimal amount)

// 局部变量（简洁有力）
List<Transaction> list = new ArrayList<>();
Map<String, Object> params = new HashMap<>();
```

### 2.3 代码规范

#### 2.3.1 Controller规范

```java
@RestController
@RequestMapping("/api/v1/transactions")
@Api(tags = "交易管理")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询交易")
    public Result<TransactionVO> getById(@PathVariable Long id) {
        return Result.success(transactionService.getById(id));
    }

    @PostMapping
    @ApiOperation("新增交易")
    @SaCheckLogin
    public Result<TransactionVO> save(@RequestBody @Valid TransactionDTO dto) {
        return Result.success(transactionService.save(dto));
    }

    @PutMapping
    @ApiOperation("更新交易")
    @SaCheckLogin
    public Result<TransactionVO> update(@RequestBody @Valid TransactionDTO dto) {
        return Result.success(transactionService.updateById(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除交易")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long id) {
        transactionService.removeById(id);
        return Result.success();
    }

    @PostMapping("/submit")
    @ApiOperation("提交审批")
    @SaCheckLogin
    public Result<Void> submit(@RequestBody @Valid IdempotencyRequest request) {
        transactionService.submit(request.getTransactionNo());
        return Result.success();
    }
}
```

#### 2.3.2 Service规范

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final IdempotencyService idempotencyService;
    private final WorkflowService workflowService;

    @Transactional(rollbackFor = Exception.class)
    public TransactionVO save(TransactionDTO dto) {
        // 1. 幂等性校验
        idempotencyService.checkIdempotency(dto.getIdempotencyKey(), dto);
        
        // 2. 数据校验
        validateTransaction(dto);
        
        // 3. 业务处理
        Transaction entity = BeanUtil.copyProperties(dto, Transaction.class);
        entity.setTransactionNo(generateTransactionNo());
        entity.setStatus(TransactionStatus.DRAFT.name());
        
        // 4. 保存数据
        transactionMapper.insert(entity);
        
        // 5. 记录审计日志
        saveAuditLog(entity, "CREATE");
        
        // 6. 标记幂等
        idempotencyService.markCompleted(dto.getIdempotencyKey(), entity);
        
        return BeanUtil.copyProperties(entity, TransactionVO.class);
    }

    private void validateTransaction(TransactionDTO dto) {
        // 业务校验逻辑
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("交易金额必须大于0");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void submit(String transactionNo) {
        // 1. 获取交易记录（带锁）
        Transaction transaction = getByTransactionNoForUpdate(transactionNo);
        
        // 2. 状态校验
        if (!TransactionStatus.DRAFT.name().equals(transaction.getStatus())) {
            throw new BusinessException("当前状态不允许提交");
        }
        
        // 3. 更新状态
        transaction.setStatus(TransactionStatus.PENDING_APPROVE.name());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.updateById(transaction);
        
        // 4. 创建审批流程
        workflowService.createWorkflow(transaction);
        
        // 5. 记录审计日志
        saveAuditLog(transaction, "SUBMIT");
    }

    private Transaction getByTransactionNoForUpdate(String transactionNo) {
        return transactionMapper.selectOne(
            new QueryWrapper<Transaction>()
                .eq("transaction_no", transactionNo)
                .last("FOR UPDATE")
        );
    }
}
```

#### 2.3.3 事务规范

```java
// 1. 读写分离场景（注解在Service层）
@Transactional(readOnly = true)
public List<Transaction> listByCondition(TransactionQuery query) {
    return transactionMapper.selectList(query);
}

// 2. 写操作（必须包含rollbackFor）
@Transactional(rollbackFor = Exception.class)
public void save(TransactionDTO dto) {
    // 业务逻辑
}

// 3. 分布式事务（跨服务场景）
@GlobalTransactional(timeoutMills = 30000, name = "saveTransaction")
public void saveWithRemote(TransactionDTO dto) {
    // 远程调用
}

// 4. 编程式事务（精细控制）
public void processWithTransaction() {
    transactionTemplate.execute(status -> {
        try {
            // 业务逻辑
            return true;
        } catch (Exception e) {
            status.setRollbackOnly();
            return false;
        }
    });
}
```

### 2.4 异常处理

```java
// 1. 业务异常（业务校验失败）
public class BusinessException extends RuntimeException {
    private String code;
    private String message;
    
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.message = message;
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}

// 2. 全局异常处理器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return Result.fail("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("SYSTEM_ERROR", "系统异常，请稍后重试");
    }
}
```

### 2.5 日志规范

```java
// 1. 操作日志（必须记录）
log.info("[{}] 用户[{}] 执行[{}]操作, 业务编号[{}]", 
    operationType, 
    getCurrentUser(), 
    entity.getClass().getSimpleName(),
    entity.getTransactionNo());

// 2. 业务日志（关键节点）
log.info("交易[{}] 提交审批, 金额[{}] {}", 
    transactionNo, 
    amount, 
    currencyCode);

// 3. 异常日志（必须包含堆栈）
log.error("交易[{}] 执行失败: {}", transactionNo, e.getMessage(), e);

// 4. 调试日志（仅开发环境）
log.debug("SQL参数: {}", JSONUtil.toJsonStr(params));
```

### 2.6 安全规范

```java
// 1. 敏感数据脱敏
@Data
public class TransactionVO {
    private String transactionNo;
    private BigDecimal amount;
    
    // 账户信息脱敏
    @SensitiveInfo(SensitiveType.BANK_ACCOUNT)
    private String accountNumber;
    
    // 金额格式化（千分位）
    @JsonFormat(pattern = "#,##0.00")
    private BigDecimal amount;
}

// 2. 密码加密存储
@Component
public class PasswordEncoder {
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}

// 3. 接口鉴权
@SaCheckLogin
@SaCheckPermission("transaction:submit")
public Result<Void> submit(Long id) {
    // 业务逻辑
}
```

---

## 三、前端代码开发规范

### 3.1 项目结构

```
web/src/
├── api/                     # API接口
│   ├── basedata/           # 基础数据模块
│   ├── dealing/            # 交易模块
│   ├── cashpool/           # 资金池模块
│   └── common.js           # 通用接口方法
├── assets/                  # 静态资源
│   ├── styles/            # 样式文件
│   └── images/            # 图片资源
├── components/             # 公共组件
├── composables/            # 组合式函数
├── router/                # 路由配置
├── store/                 # 状态管理
├── utils/                  # 工具函数
├── views/                  # 页面视图
│   ├── basedata/
│   ├── dealing/
│   ├── cashpool/
│   └── layout/
├── App.vue
└── main.js
```

### 3.2 命名规范

#### 3.2.1 文件命名

```
# 页面组件（帕斯卡命名）
TransactionList.vue         # 列表页
TransactionDetail.vue       # 详情页
TransactionEdit.vue        # 编辑页

# 公共组件（帕斯卡命名）
DataTable.vue              # 数据表格
TreeSelector.vue            # 树选择器

# 业务组件（kebab-case）
cash-pool-config.vue       # 现金池配置
auto-transfer-rule.vue     # 自动调拨规则
```

#### 3.2.2 目录命名

```
# 页面目录（与模块对应）
views/basedata/currency/    # 币种管理
views/basedata/bank/        # 银行管理

# API目录
api/dealing/transaction.js # 交易接口
api/dealing/deposit.js     # 存款接口
```

### 3.3 代码规范

#### 3.3.1 API调用规范

```javascript
// api/dealing/transaction.js
import request from '@/utils/request'

export function listTransaction(params) {
  return request({
    url: '/api/v1/transactions',
    method: 'get',
    params
  })
}

export function getTransaction(id) {
  return request({
    url: `/api/v1/transactions/${id}`,
    method: 'get'
  })
}

export function saveTransaction(data) {
  return request({
    url: '/api/v1/transactions',
    method: 'post',
    data
  })
}

export function submitTransaction(id) {
  return request({
    url: `/api/v1/transactions/${id}/submit`,
    method: 'post'
  })
}
```

#### 3.3.2 列表页规范

```vue
<template>
  <div class="transaction-list">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="交易编号" prop="transactionNo">
        <el-input v-model="queryParams.transactionNo" placeholder="请输入交易编号" />
      </el-form-item>
      <el-form-item label="交易状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="待审批" value="PENDING_APPROVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" :disabled="!selected" @click="handleBatchDelete">
          批量删除
        </el-button>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="交易编号" prop="transactionNo" width="180" />
      <el-table-column label="交易类型" prop="transactionType" width="120">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.TRANSACTION_TYPE" :value="row.transactionType" />
        </template>
      </el-table-column>
      <el-table-column label="金额" prop="amount" align="right" width="150">
        <template #default="{ row }">
          {{ formatAmount(row.amount, row.currencyCode) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createdAt" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">查看</el-link>
          <el-link type="primary" @click="handleEdit(row)">编辑</el-link>
          <el-link type="primary" @click="handleSubmit(row)">提交</el-link>
          <el-link type="danger" @click="handleDelete(row)">删除</el-link>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNo"
      v-model:page-size="queryParams.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="getList"
      @current-change="getList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTransaction, deleteTransaction } from '@/api/dealing/transaction'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const selected = ref([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  transactionNo: '',
  status: ''
})
const total = ref(0)

const getList = async () => {
  loading.value = true
  try {
    const { data } = await listTransaction(queryParams)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const handleAdd = () => {
  router.push('/dealing/transaction/edit')
}

const handleEdit = (row) => {
  router.push(`/dealing/transaction/edit?id=${row.id}`)
}

const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确认提交审批?', '提示', { type: 'warning' })
    await submitTransaction(row.id)
    ElMessage.success('提交成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该数据?', '提示', { type: 'warning' })
    await deleteTransaction(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  selected.value = selection
}

onMounted(() => {
  getList()
})
</script>
```

#### 3.3.3 表单页规范

```vue
<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="交易编号" prop="transactionNo">
          <el-input v-model="form.transactionNo" :disabled="isEdit" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="交易类型" prop="transactionType">
          <el-select v-model="form.transactionType" placeholder="请选择交易类型">
            <el-option label="存款" value="DEPOSIT" />
            <el-option label="贷款" value="LOAN" />
            <el-option label="外汇" value="FX" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    
    <!-- 动态展示 -->
    <el-row :gutter="20" v-if="form.transactionType === 'DEPOSIT'">
      <el-col :span="12">
        <el-form-item label="对手方" prop="counterpartyId">
          <counterparty-select v-model="form.counterpartyId" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="存款金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item>
      <el-button type="primary" @click="handleSubmit">保存</el-button>
      <el-button @click="handleBack">返回</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTransaction, saveTransaction, updateTransaction } from '@/api/dealing/transaction'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const isEdit = computed(() => !!route.query.id)

const form = reactive({
  id: null,
  transactionNo: '',
  transactionType: '',
  amount: 0
})

const rules = {
  transactionNo: [{ required: true, message: '请输入交易编号', trigger: 'blur' }],
  transactionType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await updateTransaction(form)
      ElMessage.success('更新成功')
    } else {
      await saveTransaction(form)
      ElMessage.success('保存成功')
    }
    handleBack()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleBack = () => {
  router.back()
}

onMounted(async () => {
  if (isEdit.value) {
    const { data } = await getTransaction(route.query.id)
    Object.assign(form, data)
  }
})
</script>
```

### 3.4 状态管理

```javascript
// store/modules/user.js
import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: null,
    permissions: []
  }),
  
  getters: {
    username: (state) => state.userInfo?.username || '',
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission)
    }
  },
  
  actions: {
    async login(username, password) {
      const { data } = await login({ username, password })
      this.token = data.token
      setToken(data.token)
    },
    
    async getUserInfo() {
      const { data } = await getUserInfo()
      this.userInfo = data.user
      this.permissions = data.permissions
    },
    
    async logout() {
      await logout()
      this.token = null
      this.userInfo = null
      this.permissions = []
      removeToken()
    }
  }
})
```

---

## 四、接口设计规范

### 4.1 RESTful API规范

#### 4.1.1 URL设计

```
# 资源命名（名词复数）
/api/v1/transactions          # 交易列表
/api/v1/transactions/{id}     # 交易详情

# 操作映射
GET    /transactions          # 查询列表
GET    /transactions/{id}    # 查询详情
POST   /transactions          # 新增
PUT    /transactions/{id}      # 更新（完整）
PATCH  /transactions/{id}     # 更新（部分）
DELETE /transactions/{id}      # 删除

# 业务操作（动词）
POST   /transactions/{id}/submit    # 提交审批
POST   /transactions/{id}/approve   # 审批通过
POST   /transactions/{id}/reject    # 审批拒绝
POST   /transactions/{id}/cancel    # 撤销
POST   /transactions/{id}/execute   # 执行交易
```

#### 4.1.2 请求参数规范

```yaml
# 查询参数（Query Parameters）
GET /api/v1/transactions?pageNo=1&pageSize=20&status=DRAFT&transactionNo=TR2026*

# 分页参数
pageNo:     integer   # 页码，默认1
pageSize:   integer   # 每页条数，默认20
sortField:  string    # 排序字段
sortOrder:  string    # 排序方向（asc/desc）

# 路径参数（Path Parameters）
GET /api/v1/transactions/123456

# 请求体（Request Body）
POST /api/v1/transactions
Content-Type: application/json

{
  "transactionType": "DEPOSIT",
  "amount": 1000000.00,
  "currencyCode": "CNY",
  "idempotencyKey": "UUID-xxx"
}
```

#### 4.1.3 响应结构

```yaml
# 成功响应
{
  "code": 0,
  "message": "success",
  "data": {
    # 业务数据
  },
  "timestamp": 1704067200000
}

# 分页响应
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "pageNo": 1,
    "pageSize": 20
  },
  "timestamp": 1704067200000
}

# 失败响应
{
  "code": "BUSINESS_ERROR",
  "message": "交易金额必须大于0",
  "data": null,
  "timestamp": 1704067200000
}

# 列表响应（无分页）
{
  "code": 0,
  "message": "success",
  "data": [],
  "timestamp": 1704067200000
}
```

### 4.2 接口安全

#### 4.2.1 鉴权认证

```yaml
# 请求头
Authorization: Bearer <token>

# 响应码
200     - 成功
401     - 未授权（token无效或过期）
403     - 无权限
404     - 资源不存在
429     - 请求过于频繁
500     - 系统异常
```

#### 4.2.2 幂等性设计

```yaml
# 幂等请求头
X-Idempotency-Key: <唯一标识>

# 幂等响应（重复请求返回原结果）
{
  "code": 0,
  "message": "success",
  "data": {
    "idempotent": true,
    "originalRequestId": "xxx"
  }
}
```

### 4.3 接口版本管理

```
# 版本控制
/api/v1/transactions     # V1版本
/api/v2/transactions     # V2版本

# 废弃标识
Deprecation: true
Sunset: Sat, 01 Jan 2027 00:00:00 GMT
```

---

## 五、通用规范

### 5.1 代码格式

```properties
# Java
indent.size=4
tab.size=4
line.separator=\n

# JavaScript
indent_size=2
tab_size=2
```

### 5.2 Git提交规范

```
# 提交格式
<type>(<scope>): <subject>

# 类型
feat:     新功能
fix:      Bug修复
docs:     文档更新
style:    代码格式
refactor: 代码重构
test:     测试
chore:    构建/辅助工具

# 示例
feat(transaction): 新增交易提交审批功能
fix(cashpool): 修复自动调拨规则执行失败问题
```

### 5.3 日志规范

```properties
# 日志级别
ERROR: 异常/错误（必须处理）
WARN:  警告（可能存在问题）
INFO:  关键业务节点
DEBUG: 开发调试信息
TRACE: 详细调试信息

# 日志格式
[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] [%level] [%logger{36}] - %msg%n
```

---

## 六、评审检查清单

### 6.1 代码评审

- [ ] 代码是否符合命名规范
- [ ] 是否包含必要的注释
- [ ] 是否处理了异常情况
- [ ] 是否有SQL注入风险
- [ ] 是否有XSS漏洞
- [ ] 敏感数据是否脱敏
- [ ] 接口是否幂等
- [ ] 事务是否正确使用
- [ ] 日志是否完整

### 6.2 接口评审

- [ ] URL是否符合RESTful规范
- [ ] 请求参数是否必填
- [ ] 响应结构是否统一
- [ ] 错误码是否明确定义
- [ ] 是否包含幂等设计
- [ ] 是否包含审计字段

### 6.3 数据库评审

- [ ] 命名是否符合规范
- [ ] 主键是否合理
- [ ] 索引是否正确
- [ ] 外键是否建立
- [ ] 审计字段是否完整
- [ ] 金额字段精度是否正确

---

*TA产出物 - 等待PM-Lead确认*
