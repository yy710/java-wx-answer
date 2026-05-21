# city-walk Node.js API

这是对原 Java/Spring Boot 工程 `adm` 与 `web` 两个模块接口面的 Node.js 移植版，入口在 `src/server.js`。

## 覆盖范围

- 后台接口：`/sys/**`、`/data/test1`
- 小程序接口：`/wx/**`、`/test/fix01`
- 微信 JS-SDK：`/sdk/get`、`/sdk/getJsapiTicket/forceRefresh`、`/sdk/create`
- 通用返回结构保持 Java 的 `CommonResult`：`{ code, message, data }`
- 后台鉴权保持 `hy-token` 风格，除 `/sys/sysUser/login`、`/sys/sysUser/logout` 外，`/sys/**` 默认需要 token
- 数据库表按 Java Entity/MyBatis XML 映射到现有 MySQL 表，分页返回 `{ records, total, size, current, pages }`

## 运行

```bash
cd nodejs
cp .env.example .env
npm install
npm start
```

默认服务地址：

```text
http://127.0.0.1:6003
```

`.env` 里默认沿用 Java 开发配置的库名和账号，可按实际环境修改：

```text
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=city-walk
DB_USER=root
DB_PASSWORD=123456
TOKEN_NAME=hy-token
```

## 说明

- 文件上传接口在 Node 版默认保存到本地 `nodejs/uploads/`，返回可访问 URL；如需完全等价腾讯云 COS 上传，可在这个封装点接入 COS SDK。
- 微信登录与 JS-SDK 依赖 `WX_MP_APP_ID`、`WX_MP_SECRET`；未传登录 `code` 时保留 Java 里的默认 `openId = 123456` 行为，便于本地联调。
- 积分时间限制按 Java 现有逻辑保留：`2026-05-21 00:01:00` 到 `2026-05-31 23:59:00`。
- 当前实现使用内存缓存代替 Redis，重启后会话和缓存会清空；生产环境建议把 `src/auth.js` 与 `src/cache.js` 换成 Redis 实现。
