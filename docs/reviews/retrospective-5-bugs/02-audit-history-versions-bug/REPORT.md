# Bug 2: Audit History `/versions` 漏查 cashflow_image

> 报告时间: 2026-07-11
> Skill: opentms-bug-fix v1.0
> 状态: ✅ 已修

## 01 - 定界
- **类别**: 后端问题
- **证据**: `tms_cashflow_image_t` 实际有 CREATE 镜像(id=7,operator='qa_tester');但 `GET /dealing/deals/AC202607110004/versions` 返回 total=0
- **结论**: DB 写入正常,controller 查询 SQL 漏

## 02 - 原因分析
- **直接**: `AuditHistoryController.listVersions` 只查 `tms_deals_image_t` 一张表,不合并 `tms_cashflow_image_t`
- **位置**: `dealing/.../AuditHistoryController.java:55-77`

## 03 - 修复
```java
// 合并 dealImage + cashflowImage
List<AuditHistoryVersionSummaryVO> records = new ArrayList<>(
    page.getRecords().stream().map(this::convertToSummary).toList());
// 补 cashflow_image 来源
List<AuditHistoryVersionSummaryVO> cfRecords = cashflowImageService
    .listByDealNumber(dealNumber, imageType, 1, pageSize).getRecords()
    .stream().map(cf -> { ... changeSummary = "现金流 " + cf.getImageType() + " · " + cf.getCflowNumber(); ... })
    .filter(cf -> records.stream().noneMatch(r -> r.getVersion() != null && r.getVersion().equals(cf.getVersion())))
    .toList();
records.addAll(cfRecords);
```
- 加 `AuditHistoryVersionSummaryVO.changeSummary` 字段
- 重新编译 dealing-1.0.0-SNAPSHOT.jar + 重启 8082

## 04 - 根因
- **研发流程**: Phase 5 BE 完成后没有"controller 跨表查询一致性"checklist
- **需求设计**: PRD §US-5 写"分页返回该交易所有镜像版本",**但没明确"哪些表算"**
- **设计规范**: CLAUDE.md 没约定"主表镜像 + 子表镜像"如何组合
- **开发实现**: 子代理只看了 `ImageService` 旧代码,没意识到 3 套镜像(deal/ac/fx) + cashflow 是独立表
- **测试工具**: 本次 dropdown 测试只测 8081,没测 dealing 端点
- **Skill**: `opentms-review-backend` checklist 缺"controller SQL 跨表覆盖度"

## 05 - 同类排查
- `DealService.findImagesByDealNumber` 类似问题:只查主表,没合并子表
- 需补 Phase 9 6 维复审"后端-前端契约"维度

## 06 - 改进措施
- **P0**: `AuditHistoryController` 加单测(每个 dealType AC/AT/FX 都覆盖)
- **P1**: Phase 5 BE 完成时强制要求"controller 涉及的所有表 + JOIN/UION 关系"清单
- **P2**: Phase 9 加 `opentms-review-contract` 新 skill
