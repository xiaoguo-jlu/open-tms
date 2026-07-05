# Checklist 02 — 前端 API 绑定

> 配合 `opentms-review-frontend` SKILL.md 使用。审核员按此清单逐项勾选。

---

## A. API 模块化 (FE-007)

### A1. 路径规范
- [ ] API 调用统一放 `web/src/api/{module}/{entity}.js`
- [ ] 函数命名 `listX / getX / saveX / updateX / deleteX / submitX / approveX / rejectX / executeX`
- [ ] 0 *.vue 直接调用 `axios` / `request.post`
- [ ] 0 业务页面写 API 路径字符串(应 import API 模块)

### A2. API 函数风格
```javascript
// 正确(Open-TMS 规范)
import request from '@/utils/request'

export function listDeal(query) {
  return request({
    url: '/api/v1/deal/page',
    method: 'get',
    params: query
  })
}

export function saveDeal(data) {
  return request({
    url: '/api/v1/deal',
    method: 'post',
    data
  })
}
```

---

## B. 接口路径 (FE-002)

### B1. 路径一致性
- [ ] URL 与 API 文档 100% 一致(`/api/v1/deal/page` 等)
- [ ] 0 路径拼写错误
- [ ] 0 路径不一致(`/deal/list` vs `/deal/page`)
- [ ] 更新 / 删除统一 POST(非 PUT / DELETE)

### B2. 参数传递
- [ ] GET 请求用 `params`(query string)
- [ ] POST 请求用 `data`(request body)
- [ ] 路径参数 `{id}` 嵌入 URL
- [ ] 0 字符串拼接 URL

---

## C. 字段绑定 (FE-002)

### C1. 字段名一致
- [ ] DTO 字段 ↔ el-form-item v-model 100% 对应
- [ ] API 响应 ↔ 表格列 100% 对应
- [ ] 0 字段名拼写错误(dealNo vs deal_no)
- [ ] 0 字段缺失(`undefined` / `null` 未处理)

### C2. 字段类型
- [ ] 日期字段用 dayjs / moment 处理(非字符串)
- [ ] 枚举字段映射 label(状态 / 类型)
- [ ] 嵌套对象字段访问安全(`obj?.field`)
- [ ] 数字字段格式化(千分位 / 精度)

---

## D. 表单标签 (FE-002)

### D1. el-form-item label
- [ ] label 与字段名一致(无错别字)
- [ ] label 完整中文
- [ ] 必填项 label 前标红 `*`
- [ ] label-width 统一(100px / 120px)

### D2. placeholder
- [ ] 输入框 placeholder 提示
- [ ] 下拉框 placeholder("请选择")
- [ ] 日期选择器 placeholder("请选择日期")
- [ ] 0 缺失 placeholder

---

## E. 响应处理 (FE-008 / FE-009)

### E1. Result 格式解析
```javascript
// 正确:request.js 已统一处理 Result<T>
{
  code: 200,
  message: "success",
  data: {...}
}

// 0 *.vue 手动判断 code === 200
// 0 *.vue 直接读 data.data
```

### E2. 错误处理
- [ ] request.js 拦截器统一处理错误码
- [ ] 401 → removeToken + router.push('/login')
- [ ] 403 → ElMessage.error('无权限')
- [ ] 500 → ElMessage.error('系统异常')
- [ ] 业务异常(400)→ ElMessage.error(message)

---

## F. 加载状态 (FE-010)

### F1. 列表加载
- [ ] el-table v-loading="loading"
- [ ] el-button :loading="submitting"
- [ ] el-button :loading="approving"
- [ ] 0 异步操作无 loading

### F2. 按钮防重复
- [ ] 提交后立即 :loading=true
- [ ] 成功后 :loading=false + 跳转
- [ ] 失败后 :loading=false + 保留表单
- [ ] 0 按钮可重复点击

---

## G. 搜索与筛选 (FE-017)

### G1. 搜索参数
- [ ] 搜索字段对应 API query 参数
- [ ] 重置按钮清空搜索条件
- [ ] 搜索按钮触发查询(非实时)
- [ ] 搜索框 300ms 防抖(可选)

### G2. 排序
- [ ] el-table @sort-change
- [ ] 排序参数传递 orderBy / orderDir
- [ ] 0 硬编码排序

---

## H. 分页 (FE-016)

### H1. el-pagination
```vue
<el-pagination
  v-model:current-page="query.pageNum"
  v-model:page-size="query.pageSize"
  :page-sizes="[10, 20, 50, 100]"
  :total="total"
  layout="total, sizes, prev, pager, next, jumper"
  @size-change="loadList"
  @current-change="loadList"
/>
```

### H2. 分页参数
- [ ] :total 绑定响应 total
- [ ] :page-sizes 包含 10/20/50/100
- [ ] size / current 变化触发查询
- [ ] 0 缺失分页

---

## I. 时间格式 (FE-019)

### I1. 统一格式化
```javascript
// 正确:dayjs + formatDate util
import { formatDate } from '@/utils/date'

formatDate(row.createdAt, 'YYYY-MM-DD HH:mm:ss')
// → "2026-07-05 14:30:00"
```

### I2. 显示规范
- [ ] 时间字段 `YYYY-MM-DD HH:mm:ss`
- [ ] 日期字段 `YYYY-MM-DD`
- [ ] 0 ISO 字符串直接显示
- [ ] 0 时区混乱

---

## J. 金额格式 (FE-018 / FE-020 / FE-021)

### J1. 千分位
- [ ] 金额字段千分位显示
- [ ] 汇率 8 位小数
- [ ] 利率 4 位小数 + %
- [ ] 高精度 AC Deal 用 AmountDisplay

### J2. 货币符号
- [ ] 金额字段显示对应币种(¥ / $ / €)
- [ ] 币种对显示(USD/CNY)
- [ ] 0 单一币种显示

---

## 审核结论

通过项数 / 总项数 = ____%

| 等级 | 通过率 |
|------|--------|
| A | ≥95% |
| B | ≥85% |
| C | ≥70% |
| D | <70% |

**额外扣分项**:
- 任何 FE-002 / FE-007 / FE-008 / FE-009 / FE-013 / FE-014 / FE-016 (P0) 未通过 → 直接降至 C
- 3 个 P0 未通过 → 直接降至 D