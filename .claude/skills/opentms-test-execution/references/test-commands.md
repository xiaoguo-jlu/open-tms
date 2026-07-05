# 测试工具命令速查

> 从 SKILL.md 附录C移出。

## 测试运行器（主入口）

```bash
# 查看可用测试套件
python .agents/skills/opentms-test-execution/scripts/run_tests.py list

# 运行指定套件
python .agents/skills/opentms-test-execution/scripts/run_tests.py run basedata

# 运行全部
python .agents/skills/opentms-test-execution/scripts/run_tests.py run all

# 停止测试进程
python .agents/skills/opentms-test-execution/scripts/run_tests.py stop

# 查看报告
python .agents/skills/opentms-test-execution/scripts/run_tests.py report basedata
```

## 手动快速验证（仅快速探路用）

```bash
# 单接口验证
curl -X GET "http://localhost:8081/api/v1/currencies"
curl -X POST "http://localhost:8081/api/v1/currencies" \
  -H "Content-Type: application/json" \
  -d '{"code":"USD","name":"美元"}'
```

## 前置条件
- Python 3.8+
- requests库已安装：`pip install requests`
- 后端服务必须运行在端口8081

## 测试套件说明

| 套件 | 说明 | 测试文件 |
|------|------|----------|
| basedata | 币种/国家/银行等API | test_all_post.py |
| ac | 实际现金流API | test_ac_api.py |
| at | 账户转账API | test_at_api.py |
| full | 完整测试套件 | test_full.py |
