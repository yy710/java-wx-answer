# Java J-12/J-13 验收、上线与回滚清单

## J-12 微信真机验收

1. 使用测试公众号和脱敏测试账号登录，确认 `Asia/Shanghai` 日期与服务端一致。
2. 在微信 H5 打开“每日任务”，验证答题开始后退出、刷新、超时都不能重新抽题。
3. 正常观看锁定视频，暂停、拖动、倍速、后台隐藏和断网后恢复，确认服务端只累计可信心跳增量。
4. 打开一条启用的民生实事，内容成功显示后点击“查看完成，领取10积分”；停用内容后确认申领失败。
5. 生成已发布模板海报，验证中文、二维码、头像/昵称白名单字段和COS图片下载；用相同输入验证7天缓存命中。
6. 在微信中完成保存或分享后，点击页面显式“领取保存/分享积分”；确认微信分享配置的 `success` 回调不会直接发积分。
7. 用抓包或后端日志核对每项响应保留 `CommonResult.data` 包装和 `requestedPoints/awardedPoints/awardReason/dailyRemainingPoints/walletPoints` 字段。
8. 用管理员账号打开每日任务设置和海报模板页面，验证版本冲突、模板非法字段、外部URL和未发布模板均被拒绝。

验收证据必须包括：测试账号、请求时间、接口响应、MySQL 流水/幂等/任务记录、微信真机截图和失败重现步骤。公众号、COS、字体、域名或分享行为与文档不一致时立即停止。

## J-13 上线

1. 重新执行 `git status --short --branch`，只确认本次任务文件进入提交清单；保留既有 CSV、备份、脚本、`.DS_Store` 和 `graphify-out/` 改动。
2. 在非生产库执行：
   ```bash
   CITY_WALK_MIGRATION_TARGET=NON_PRODUCTION \
   MYSQL_HOST=... MYSQL_PORT=3306 MYSQL_DATABASE=... MYSQL_USER=... MYSQL_PWD=... \
   ./scripts/daily_points_poster_migration.sh inspect
   ```
3. 核对数据库主机不是生产库，保存表记录数、历史正向流水重复检查和配置快照。
4. 执行 `apply`，再执行 `verify`；迁移前后旧表记录数不一致、唯一索引冲突或配置缺失立即停止。
5. 发布 Java web/adm 服务和 H5 构建；总开关保持 `enabled=0`，先录入题库、可信视频时长、已发布模板和COS素材。
6. 按 J-12 完成灰度真机验收后，由授权运营人员把总开关和四项子开关逐项打开。
7. 观察奖励流水、`reward_claims`、每日任务记录、Redis回退日志、COS失败率和接口错误码；正向奖励异常时立即关闭总开关。

## 回滚

- 业务回滚：管理端将总开关设为0；保留已有钱包流水和任务审计记录，不通过回滚扣减用户积分。
- 代码回滚：停止新版本，恢复上一版 Java web/adm/H5 制品；不得删除数据库记录。
- 数据库回滚：仅在迁移演练或经审批的维护窗口执行 `daily_points_poster_migration.sh rollback`，执行前保存备份并确认没有新功能流水；生产有新流水时停止并转人工数据修复评审。
- 回滚后执行 `verify`、登录、旧答题、旧视频、风险阅读、钱包查询和负积分消费回归；任一不一致都不能宣布完成。

## 立即停止条件

工作树基线不符、生产数据库被配置为目标、迁移记录数变化、历史唯一键冲突、题库不足、视频无可信时长、Sharp/Batik/COS/中文字体不兼容、微信真机分享行为不符、或测试需要删除现有用户数据时，立即停止并报告，不得猜测修复。

## 本次执行验证证据

- 已执行 `git status --short --branch`，Java、旧 H5、Vue 管理端的既有改动均已识别；`nodejs` 和 `nodejs-sqlite-single-service` 未修改。
- 已执行 `bash -n scripts/daily_points_poster_migration.sh`，迁移脚本语法通过；未连接生产数据库，也未执行 apply/rollback。
- 已执行旧 H5 新增 API 模块的 Node 18 语法检查和三仓库 `git diff --check`；通过，CRLF 提示不影响检查结果。
- Vue 管理端在 Node 18.20.8 下执行 `npm run build` 通过；仅有既有的压缩器、组件命名、`:deep` 弃用和 chunk 体积警告。
- Java 构建未宣称通过：当前机器 `java -version`/`javac -version` 均为 1.8.0_152，Gradle 8.9 在解析 Spring Boot 3.1.0 时停止并报告“依赖至少需要 JVM 17”。进入 J-11/J-12 前必须切换到 Java 17+，再运行 `gradle test`、Testcontainers、COS 集成和微信真机验收。
