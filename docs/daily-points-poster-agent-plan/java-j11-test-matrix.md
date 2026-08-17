# Java J-11 验证矩阵

本矩阵只用于 `city-walk-java` 的 Java + MySQL + Redis 方案。测试环境必须是脱敏的非生产 MySQL、独立 Redis 和测试 COS；禁止用生产账号、生产用户或生产积分钱包。每一项执行前记录测试用户、配置版本和数据库快照，执行后按“清理方式”回收测试数据。

| 编号 | 初始数据 | 操作步骤 | 预期数据库变化 | 预期接口响应 | 预期前端显示 | 清理方式 |
|---|---|---|---|---|---|---|
| J11-01 | 启用题库不少于2题，钱包0 | start→两题全错→submit | 会话SUBMITTED，题目均不奖励，任务记录0分 | completed=true，awarded=0 | 显示答题完成0分 | 删除测试用户每日会话 |
| J11-02 | 同上 | 两题答对1题 | 1条事件9成功流水，1个幂等占位 | awarded=10.00 | 显示10分 | 删除用户流水与占位 |
| J11-03 | 同上 | 两题答对 | 2条独立事件9流水 | awarded=20.00 | 显示20分 | 删除用户流水与占位 |
| J11-04 | 已有quiz会话STARTED | 再次start | 不新增会话 | DAILY_CLAIMED | 禁止重新抽题 | 删除会话 |
| J11-05 | 已开始答题 | 刷新并提交原session | 原会话只结算一次 | 重复提交返回ALREADY_CLAIMED | 保持完成态 | 删除会话 |
| J11-06 | 会话deadline已过 | submit | 会话EXPIRED，任务记录完成0分 | QUIZ_EXPIRED | 显示已过期 | 删除会话 |
| J11-07 | 启用题目少于配置数 | start | 不写入会话 | QUIZ_POOL_INSUFFICIENT | 提示运营配置错误 | 补齐题库/删除测试数据 |
| J11-08 | 日期切换前有昨日会话 | 跨Asia/Shanghai自然日status/start | 新日期可建立新会话 | 新taskDate | 显示新一天任务 | 删除两日会话 |
| J11-09 | 视频可信时长120秒 | start | 锁定1条视频及120秒快照 | 返回required=60 | 显示锁定视频 | 删除视频会话 |
| J11-10 | 视频会话STARTED | 正常每5秒上报位置 | credited按增量累计，版本递增 | 200/最新version | 进度递增 | 删除视频会话 |
| J11-11 | 同上 | 跳播超过15秒 | 不更新累计和版本 | VIDEO_HEARTBEAT_CONFLICT | 提示继续正常观看 | 删除视频会话 |
| J11-12 | 同上 | 心跳间隔超过60秒 | 不更新累计 | VIDEO_HEARTBEAT_CONFLICT | 提示恢复播放 | 删除视频会话 |
| J11-13 | 同上 | 使用旧version并发提交 | 只有一个更新成功 | 一个成功、一个冲突 | 前端刷新进度 | 删除视频会话 |
| J11-14 | credited达到required | claim | 1条事件10流水、任务CLAIMED | awarded=20.00 | 显示领取完成 | 删除流水/任务 |
| J11-15 | 视频无duration | start | 不写会话 | VIDEO_DURATION_UNTRUSTED | 提示运营补时长 | 修复素材标记 |
| J11-16 | 启用民生实事 | affair/claim一次 | 1条事件11流水、唯一任务记录 | awarded=10.00 | 显示领取完成 | 删除流水/任务 |
| J11-17 | 同一用户已有民生任务 | 再次claim | 无新增流水 | ALREADY_CLAIMED | 保持完成态 | 删除流水/任务 |
| J11-18 | 民生内容已停用 | claim | 无新增数据 | AFFAIR_NOT_FOUND | 提示内容不可用 | 恢复内容状态 |
| J11-19 | 已发布模板和COS素材 | list→generate | 新增SUCCESS生成记录 | 返回PNG地址与7天过期 | 显示服务端海报 | 删除生成记录/COS对象 |
| J11-20 | 同输入生成记录未过期 | 再次generate | 不新增记录 | cached=true | 使用缓存 | 删除生成记录/COS对象 |
| J11-21 | 模板换版本 | 用新version生成 | 新增缓存键 | cached=false | 显示新模板 | 删除生成记录 |
| J11-22 | 模板含script/expression | 管理端preview/save | 不写入模板 | POSTER_SCHEMA_INVALID | 显示校验错误 | 删除草稿 |
| J11-23 | 素材超过10MB | generate | 生成失败，不写SUCCESS | POSTER_RENDER_FAILED | 显示失败 | 删除素材 |
| J11-24 | 素材为公网URL | save/generate | 不接受/不加载公网 | schema或render失败 | 显示素材不受信任 | 删除草稿 |
| J11-25 | share token未过期 | prepare→显式claim | 令牌used_at、事件12流水、任务完成 | awarded=10.00 | 显示10分 | 删除令牌/流水 |
| J11-26 | token已过期 | claim | 不更新 | ACTION_TOKEN_EXPIRED | 提示重新准备 | 删除令牌 |
| J11-27 | token跨用户 | 用户B claim | 不更新 | ACTION_TOKEN_MISMATCH | 拒绝领取 | 删除令牌 |
| J11-28 | token已使用 | 重复claim | 不新增 | ACTION_TOKEN_REUSED | 显示已使用 | 删除令牌 |
| J11-29 | SAVE后再SHARE | 同日完成两动作 | 只有1条事件12和任务记录 | 第二次ALREADY_CLAIMED | 只奖励一次 | 删除流水/任务 |
| J11-30 | 今日正向已55分 | 申领10分任务 | 流水实际5分，任务仍完成 | PARTIAL_DAILY_CAP/5.00 | 显示5分 | 删除流水/任务 |
| J11-31 | 钱包3995，日上限充足 | 申领10分任务 | 钱包到4000，流水5分 | PARTIAL_WALLET_CAP | 显示5分 | 删除流水/任务 |
| J11-32 | 钱包3995且今日55分 | 申领10分 | 只入5分，两个上限同时满足 | DAILY_AND_WALLET_CAP或部分原因 | 显示5分 | 删除流水/任务 |
| J11-33 | 钱包达到4000 | 消费后再次申领 | 消费负流水不减少日正向累计，不再奖励 | awarded=0，任务完成 | 显示0分且已完成 | 删除测试流水 |
| J11-34 | 旧签到/邀请/风险/旧视频正向流水 | 新任务申领 | sumPositiveToday包含旧事件 | 实际按60封顶 | 显示统一剩余 | 删除测试流水 |
| J11-35 | 两个相同reward请求并发 | 同一event并发POST | 一个reward_claim和一条钱包流水 | 两次幂等结果 | 只显示一次到账 | 删除占位/流水 |
| J11-36 | Redis可用 | 更新配置 | MySQL版本+1，Redis失效 | 返回新版本 | 页面更新 | 恢复默认配置 |
| J11-37 | Redis停止 | status/generate | 只读MySQL成功 | 正常响应 | 页面正常 | 启动Redis |
| J11-38 | 管理员A读版本1，B先更新 | A提交版本1 | A更新0行并冲突 | CONFIG_VERSION_CONFLICT | 提示刷新 | 恢复默认配置 |
| J11-39 | 非管理员token | 调用/sys配置和模板 | 无业务写入 | 401/403 | 无权限提示 | 清理会话 |
| J11-40 | 功能总开关关闭 | 调用所有任务入口 | 无会话/流水/令牌新增 | DAILY_TASK_DISABLED | 页面显示关闭 | 保持关闭 |
| J11-41 | 备份文件和迁移前记录数 | apply→verify | 表、列、唯一索引存在且记录数可追溯 | verify无异常 | 发布人可核对 | 回滚脚本仅在演练库执行 |
| J11-42 | 迁移演练库 | rollback→verify | 新表移除，旧表字段恢复 | verify无异常 | 功能不可用 | 丢弃演练库 |

进入 J-12 前，J11-01~42 必须有数据库、接口和前端截图/日志证据；任何一项失败立即停止，不得跳过或修改预期结果。
