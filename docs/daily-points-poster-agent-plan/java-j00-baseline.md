# J-00 Java 基线核验

核验日期：2026-08-12（Asia/Shanghai）

## 工作树与分支

本任务只允许修改 `city-walk-java`。`city-walk-h5` 和 `city-walk-manage` 是配套前端工作树；`nodejs`、`nodejs-sqlite-single-service` 不属于本任务。

执行前命令：

```bash
git status --short --branch
```

核验结果：

```text
## develop...github/develop [ahead 1]
 M scripts/金融知识问答题库(202603第二批)判断题.csv
 M web/.DS_Store
?? graphify-out/
?? scripts/answer_backup/
?? scripts/export_database_snapshot.py
?? scripts/repair_answer_ids.py
```

以上文件均为既有工作树改动或既有未跟踪内容。本任务不覆盖、不删除、不暂存这些内容。

## 构建与运行时核验

Java 仓库包含 `build.gradle`、`web/build.gradle`、`common/build.gradle`、`adm/build.gradle` 和 `settings.gradle`，未提供 `gradlew`。执行 `./gradlew --version` 的结果为 `no such file or directory`，因此后续构建必须使用已安装的 Gradle 命令，并在进入 J-01 前记录 `java -version`、`gradle --version`、MySQL/Redis 客户端版本。

## 数据库安全边界

- 不连接生产数据库执行迁移或测试。
- 迁移先执行 inspect，再执行 apply；apply 前保存备份和记录数。
- 发现历史唯一键冲突、生产连接、记录数不一致或无法区分的用户改动时立即停止。

## 图谱核验

Java 图谱已在本地生成，结果为 429 个文件、3725 个节点、10049 条边。图谱输出目录 `graphify-out/` 不属于本任务提交内容，保持原样。

## 进入下一阶段条件

只有在本文件与当前 `git status --short --branch` 一致、确认测试数据库连接、并完成 J-01 inspect 后，才允许执行数据库 apply。
