const fs = require('fs');
const http = require('http');
const path = require('path');
const { URL } = require('url');

const auth = require('./auth');
const cache = require('./cache');
const config = require('./config');
const db = require('./db');
const result = require('./result');
const services = require('./services');
const { UnauthorizedError, YunKeError } = require('./errors');
const { extFromFilename, pageObject, snowflakeId, todayString, toNumber } = require('./utils');

const routes = [];
const uploadRoot = path.join(__dirname, '..', 'uploads');

function add(method, routePath, handler, options = {}) {
  routes.push({ method, path: routePath, handler, auth: !!options.auth });
}

function body(ctx) {
  return ctx.body && typeof ctx.body === 'object' ? ctx.body : {};
}

function query(ctx, key, fallback = undefined) {
  return ctx.query[key] == null ? fallback : ctx.query[key];
}

function ok(data = null) {
  return result.ok(data);
}

function okStatus(flag = true) {
  return result.status(!!flag);
}

function setCors(res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,PATCH,DELETE,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, hy-token, token');
}

function sendJson(res, payload) {
  setCors(res);
  res.statusCode = 200;
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  res.end(JSON.stringify(payload));
}

async function readBuffer(req) {
  const chunks = [];
  for await (const chunk of req) {
    chunks.push(chunk);
  }
  return Buffer.concat(chunks);
}

function parseMultipart(buffer, contentType) {
  const boundaryMatch = /boundary=([^;]+)/i.exec(contentType || '');
  if (!boundaryMatch) {
    return {};
  }
  const boundary = `--${boundaryMatch[1]}`;
  const raw = buffer.toString('binary');
  const parts = raw.split(boundary).filter((part) => part.includes('Content-Disposition'));
  const files = {};
  const fields = {};
  for (const part of parts) {
    const splitAt = part.indexOf('\r\n\r\n');
    if (splitAt < 0) {
      continue;
    }
    const header = part.slice(0, splitAt);
    let content = part.slice(splitAt + 4);
    content = content.replace(/\r\n--$/, '').replace(/\r\n$/, '');
    const nameMatch = /name="([^"]+)"/.exec(header);
    if (!nameMatch) {
      continue;
    }
    const filenameMatch = /filename="([^"]*)"/.exec(header);
    if (filenameMatch) {
      files[nameMatch[1]] = {
        filename: filenameMatch[1],
        buffer: Buffer.from(content, 'binary')
      };
    } else {
      fields[nameMatch[1]] = Buffer.from(content, 'binary').toString('utf8');
    }
  }
  return { fields, files };
}

async function parseBody(req) {
  const method = req.method.toUpperCase();
  if (method === 'GET' || method === 'HEAD' || method === 'OPTIONS') {
    return {};
  }
  const buffer = await readBuffer(req);
  if (!buffer.length) {
    return {};
  }
  const contentType = req.headers['content-type'] || '';
  if (contentType.includes('multipart/form-data')) {
    return parseMultipart(buffer, contentType);
  }
  if (contentType.includes('application/x-www-form-urlencoded')) {
    return Object.fromEntries(new URLSearchParams(buffer.toString('utf8')));
  }
  try {
    return JSON.parse(buffer.toString('utf8'));
  } catch (error) {
    return {};
  }
}

function matchRoute(method, pathname) {
  return routes.find((route) => route.method === method && route.path === pathname);
}

async function serveUpload(req, res, pathname) {
  const relative = decodeURIComponent(pathname.replace(/^\/uploads\//, ''));
  const filePath = path.resolve(uploadRoot, relative);
  if (!filePath.startsWith(uploadRoot)) {
    res.statusCode = 403;
    res.end('Forbidden');
    return true;
  }
  if (!fs.existsSync(filePath) || !fs.statSync(filePath).isFile()) {
    return false;
  }
  fs.createReadStream(filePath).pipe(res);
  return true;
}

async function handleUpload(ctx) {
  const file = ctx.body.files && ctx.body.files.file;
  if (!file) {
    return result.failed('上传失败');
  }
  const dateDir = todayString();
  const dir = path.join(uploadRoot, dateDir);
  fs.mkdirSync(dir, { recursive: true });
  const filename = `${snowflakeId()}${extFromFilename(file.filename)}`;
  const filePath = path.join(dir, filename);
  fs.writeFileSync(filePath, file.buffer);
  return ok(`${config.publicFileBaseUrl.replace(/\/$/, '')}/uploads/${dateDir}/${filename}`);
}

function cosPolicy() {
  const now = Math.floor(Date.now() / 1000);
  return {
    tmpSecretId: config.cos.accessKey || null,
    tmpSecretKey: config.cos.secretKey || null,
    sessionToken: null,
    startTime: now,
    expiredTime: now + 1800,
    bucket: config.cos.bucket || null,
    region: config.cos.region || null
  };
}

function registerGenericCrud(base, modelKey, options = {}) {
  if (options.list) {
    add('POST', `${base}/list`, async (ctx) => ok(await db.selectList(modelKey, db.filtersFromData(modelKey, body(ctx)), { orderBy: options.orderBy })), { auth: true });
  }
  if (options.page) {
    add('POST', `${base}/page`, async (ctx) => ok(await db.page(modelKey, body(ctx), { orderBy: options.orderBy, likeFields: options.likeFields })), { auth: true });
  }
  if (options.get) {
    add('GET', `${base}/getOneById`, async (ctx) => ok(await db.getById(modelKey, query(ctx, 'id'))), { auth: true });
  }
  if (options.save) {
    add('POST', `${base}/addOrModify`, async (ctx) => {
      await db.saveOrUpdate(modelKey, body(ctx));
      return okStatus(true);
    }, { auth: true });
  }
  if (options.remove) {
    add('DELETE', `${base}/delete`, async (ctx) => okStatus(await db.deleteByIds(modelKey, ctx.body || [])), { auth: true });
  }
}

function registerAdminRoutes() {
  registerGenericCrud('/sys/agreement', 'agreement', { list: true, page: true, get: true, save: true, remove: true });
  registerGenericCrud('/sys/appNotice', 'appNotice', { list: true, page: true, get: true, save: true, remove: true, orderBy: 'sort ASC' });
  registerGenericCrud('/sys/category', 'category', { list: true, page: true, get: true, save: true, remove: true, orderBy: 'seq ASC' });
  registerGenericCrud('/sys/customerServer', 'customServer', { page: true, get: true, save: true, remove: true });
  registerGenericCrud('/sys/introduce', 'introduce', { page: true, get: true, save: true, remove: true, orderBy: 'seq ASC' });
  registerGenericCrud('/sys/inviteSet', 'inviteSet', { page: true, get: true, save: true, remove: true });
  registerGenericCrud('/sys/rewardSet', 'rewardSet', { page: true, get: true, save: true, remove: true });
  registerGenericCrud('/sys/topicLine', 'topicLine', { page: true, get: true, save: true, remove: true, orderBy: 'seq ASC' });
  registerGenericCrud('/sys/video', 'video', { page: true, get: true, save: true, remove: true, orderBy: 'seq ASC' });
  registerGenericCrud('/sys/topicRecordTopic', 'topicRecordTopic', { page: true, get: true, save: true, remove: true });

  add('POST', '/sys/payment/page', async (ctx) => {
    const page = await db.page('payment', body(ctx));
    page.records = page.records.map((record) => ({ ...record, qrImg: `data:text/plain;base64,${Buffer.from(String(record.id)).toString('base64')}` }));
    return ok(page);
  }, { auth: true });
  add('GET', '/sys/payment/getOneById', async (ctx) => ok(await db.getById('payment', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/payment/addOrModify', async (ctx) => okStatus(await db.saveOrUpdate('payment', body(ctx))), { auth: true });
  add('DELETE', '/sys/payment/delete', async (ctx) => okStatus(await db.deleteByIds('payment', ctx.body || [])), { auth: true });

  add('GET', '/sys/area/listByPid', async (ctx) => ok(await db.selectList('area', [{ prop: 'parentId', value: query(ctx, 'pid') }], { orderBy: 'area_id ASC' })), { auth: true });
  add('POST', '/sys/area/page', async (ctx) => ok(await db.page('area', body(ctx), { orderBy: 'area_id ASC' })), { auth: true });
  add('POST', '/sys/area/list', async (ctx) => ok(await db.selectList('area', db.filtersFromData('area', body(ctx)), { orderBy: 'area_id ASC' })), { auth: true });
  add('GET', '/sys/area/getAreaTree', async () => ok(await services.areaTree()), { auth: true });

  add('POST', '/sys/dataAnalysis/indexData', async () => ok(await services.dataAnalysis()), { auth: true });
  add('GET', '/data/test1', async () => ok(null));

  add('POST', '/sys/feedback/page', async (ctx) => ok(await services.feedbackPage(body(ctx))), { auth: true });
  add('GET', '/sys/feedback/getOneById', async (ctx) => ok(await db.getById('feedback', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/feedback/modify', async (ctx) => {
    await db.updateById('feedback', body(ctx).id, { handelResponse: body(ctx).handelResponse, handleFlag: true, updateTime: new Date() });
    return ok();
  }, { auth: true });
  add('DELETE', '/sys/feedback/delete', async (ctx) => okStatus(await db.deleteByIds('feedback', ctx.body || [])), { auth: true });

  add('POST', '/sys/introduceSet/page', async (ctx) => ok(pageObject([cache.get('introduceSet') || {}], 1, body(ctx).pageNum || 1, body(ctx).pageSize || 15)), { auth: true });
  add('POST', '/sys/introduceSet/addOrModify', async (ctx) => {
    cache.set('introduceSet', body(ctx));
    return ok();
  }, { auth: true });

  add('POST', '/sys/riskWarning/page', async (ctx) => ok(await services.riskWarningPage(body(ctx))), { auth: true });
  add('GET', '/sys/riskWarning/getOneById', async (ctx) => ok(await db.getById('riskWarning', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/riskWarning/addOrModify', async (ctx) => okStatus(await db.saveOrUpdate('riskWarning', body(ctx))), { auth: true });
  add('DELETE', '/sys/riskWarning/delete', async (ctx) => okStatus(await db.deleteByIds('riskWarning', ctx.body || [])), { auth: true });
  add('POST', '/sys/riskWarningRecord/page', async (ctx) => ok(await services.riskWarningRecordPage(body(ctx))), { auth: true });

  add('POST', '/sys/signActivity/page', async (ctx) => ok(await db.page('signActivity', body(ctx))), { auth: true });
  add('GET', '/sys/signActivity/getOneById', async (ctx) => ok(await db.getById('signActivity', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/signActivity/addOrModify', async (ctx) => {
    await services.addOrModifyTimedActivity('signActivity', body(ctx), { cacheKey: 'signActivity' });
    return ok();
  }, { auth: true });
  add('DELETE', '/sys/signActivity/delete', async (ctx) => {
    cache.del('signActivity');
    return okStatus(await db.deleteByIds('signActivity', ctx.body || []));
  }, { auth: true });

  add('POST', '/sys/sysLogInfo/page', async (ctx) => ok(await db.page('sysLogInfo', body(ctx), { orderBy: 'create_time DESC' })), { auth: true });
  add('POST', '/sys/sysLogInfo/saveOrUpdata', async (ctx) => okStatus(await db.saveOrUpdate('sysLogInfo', body(ctx))), { auth: true });
  add('POST', '/sys/sysLogInfo/list', async (ctx) => ok(await db.selectList('sysLogInfo', db.filtersFromData('sysLogInfo', body(ctx)), { orderBy: 'create_time DESC' })), { auth: true });
  add('POST', '/sys/sysLogInfo/details', async (ctx) => ok(await db.selectOne('sysLogInfo', db.filtersFromData('sysLogInfo', body(ctx)))), { auth: true });
  add('POST', '/sys/sysLogInfo/remove', async (ctx) => okStatus(await db.deleteByIds('sysLogInfo', ctx.body || [])), { auth: true });

  add('GET', '/sys/sysPermission/list', async () => ok(await services.permissionTree()), { auth: true });
  add('GET', '/sys/sysPermission/lazyList', async (ctx) => ok(await db.selectList('sysPermission', [{ prop: 'pid', value: query(ctx, 'pid') }], { orderBy: 'sort ASC' })), { auth: true });
  add('GET', '/sys/sysPermission/Treelist', async (ctx) => ok(await services.permissionTree(auth.getLoginId(ctx.req))), { auth: true });
  add('GET', '/sys/sysPermission/listByPid', async (ctx) => ok(await services.permissionTreeByPid(query(ctx, 'pid'))), { auth: true });
  add('POST', '/sys/sysPermission/savaOrupdate', async (ctx) => okStatus(await db.saveOrUpdate('sysPermission', body(ctx))), { auth: true });
  add('POST', '/sys/sysPermission/delete', async (ctx) => {
    await services.deletePermission(query(ctx, 'id'));
    return ok();
  }, { auth: true });
  add('POST', '/sys/sysPermission/batchsavaOrupdate', async (ctx) => {
    for (const item of ctx.body || []) {
      await db.saveOrUpdate('sysPermission', item);
    }
    return ok();
  }, { auth: true });

  add('POST', '/sys/sysRole/page', async (ctx) => ok(await db.page('sysRole', body(ctx))), { auth: true });
  add('GET', '/sys/sysRole/listAll', async () => ok(await db.selectList('sysRole')), { auth: true });
  add('POST', '/sys/sysRole/addOrModify', async (ctx) => okStatus(await db.saveOrUpdate('sysRole', body(ctx))), { auth: true });
  add('DELETE', '/sys/sysRole/delete', async (ctx) => {
    await services.deleteSysRoles(ctx.body || []);
    return ok();
  }, { auth: true });
  add('GET', '/sys/sysRole/listByRoleId', async (ctx) => ok(await services.listPermissionByRole(query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/sysRole/setPermission', async (ctx) => {
    await services.setRolePermission(body(ctx));
    return ok();
  }, { auth: true });

  add('POST', '/sys/sysUser/login', async (ctx) => ok(await services.sysLogin(body(ctx))));
  add('GET', '/sys/sysUser/logout', async (ctx) => {
    auth.logout(ctx.req);
    return ok();
  });
  add('POST', '/sys/sysUser/add', async (ctx) => {
    await services.addSysUser(body(ctx));
    return ok();
  }, { auth: true });
  add('POST', '/sys/sysUser/updatePassword', async (ctx) => {
    await db.updateById('sysUser', body(ctx).id, { password: require('./utils').md5(body(ctx).password) });
    return ok();
  }, { auth: true });
  add('GET', '/sys/sysUser/getOneById', async (ctx) => ok(await db.getById('sysUser', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/sysUser/modify', async (ctx) => {
    await services.modifySysUser(body(ctx));
    return ok();
  }, { auth: true });
  add('GET', '/sys/sysUser/selectAll', async () => ok(await db.selectList('sysUser')), { auth: true });
  add('DELETE', '/sys/sysUser/delete', async (ctx) => {
    await services.deleteSysUsers(ctx.body || []);
    return ok();
  }, { auth: true });
  add('POST', '/sys/sysUser/page', async (ctx) => ok(await services.sysUserPage(body(ctx))), { auth: true });
  add('POST', '/sys/sysUser/setRole', async (ctx) => {
    await services.setUserRole(body(ctx));
    return ok();
  }, { auth: true });
  add('POST', '/sys/sysUser/deleteRole', async (ctx) => {
    await services.deleteUserRole(body(ctx));
    return ok();
  }, { auth: true });

  add('POST', '/sys/ticketActivity/page', async (ctx) => ok(await db.page('ticketActivity', body(ctx))), { auth: true });
  add('GET', '/sys/ticketActivity/getOneById', async (ctx) => ok(await db.getById('ticketActivity', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/ticketActivity/addOrModify', async (ctx) => {
    await services.addOrModifyTimedActivity('ticketActivity', body(ctx), { cacheKey: 'ticketActivity' });
    return okStatus(true);
  }, { auth: true });
  add('DELETE', '/sys/ticketActivity/delete', async (ctx) => {
    cache.del('ticketActivity');
    return okStatus(await db.deleteByIds('ticketActivity', ctx.body || []));
  }, { auth: true });
  add('POST', '/sys/ticketActivity/setVideo', async (ctx) => okStatus(await services.setTicketVideos(body(ctx))), { auth: true });
  add('GET', '/sys/ticketActivity/getVideo', async (ctx) => ok(await services.getTicketVideos(query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/ticketRecord/page', async (ctx) => ok(await services.ticketRecordPage(body(ctx))), { auth: true });

  add('POST', '/sys/topicActivity/page', async (ctx) => ok(await db.page('topicActivity', body(ctx))), { auth: true });
  add('GET', '/sys/topicActivity/getOneById', async (ctx) => ok(await db.getById('topicActivity', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/topicActivity/addOrModify', async (ctx) => {
    await services.addOrModifyTimedActivity('topicActivity', body(ctx), { cacheKey: 'topicActivity' });
    return okStatus(true);
  }, { auth: true });
  add('DELETE', '/sys/topicActivity/delete', async (ctx) => {
    cache.del('topicActivity');
    return okStatus(await db.deleteByIds('topicActivity', ctx.body || []));
  }, { auth: true });

  add('POST', '/sys/topic/page', async (ctx) => ok(await db.page('topic', body(ctx))), { auth: true });
  add('GET', '/sys/topic/getOneById', async (ctx) => ok(await services.topicWithItems(query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/topic/addOrModify', async (ctx) => {
    await services.saveTopic(body(ctx));
    return ok();
  }, { auth: true });
  add('DELETE', '/sys/topic/delete', async (ctx) => {
    await services.deleteTopics(ctx.body || []);
    return okStatus(true);
  }, { auth: true });

  add('POST', '/sys/topicRecord/page', async (ctx) => ok(await services.topicRecordPage(body(ctx))), { auth: true });
  add('GET', '/sys/topicRecord/getOneById', async (ctx) => ok(await services.topicRecordDetail(query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/topicRecordSingle/page', async (ctx) => ok(await services.topicRecordSinglePage(body(ctx))), { auth: true });
  add('GET', '/sys/topicRecordSingle/getOneById', async (ctx) => ok(await services.singleRecordDetail(query(ctx, 'id'))), { auth: true });

  add('POST', '/sys/user/page', async (ctx) => ok(await db.page('user', body(ctx))), { auth: true });
  add('GET', '/sys/user/getOneById', async (ctx) => ok(await db.getById('user', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/user/modify', async (ctx) => ok(), { auth: true });
  add('POST', '/sys/userMulti/page', async (ctx) => ok(pageObject([], 0, body(ctx).pageNum || 1, body(ctx).pageSize || 15)), { auth: true });
  add('POST', '/sys/userMulti/addOrModify', async (ctx) => {
    cache.set('userMulti', body(ctx).userMultiple);
    return ok();
  }, { auth: true });
  add('POST', '/sys/userMultiaddOrModify', async (ctx) => {
    cache.set('userMulti', body(ctx).userMultiple);
    return ok();
  }, { auth: true });
  add('POST', '/sys/userWallet/page', async (ctx) => ok(await services.userWalletPage(body(ctx))), { auth: true });
  add('POST', '/sys/userWalletRecord/page', async (ctx) => ok(await services.userWalletRecordPage(body(ctx))), { auth: true });

  add('POST', '/sys/videoActivity/page', async (ctx) => ok(await db.page('videoActivity', body(ctx))), { auth: true });
  add('GET', '/sys/videoActivity/getOneById', async (ctx) => ok(await db.getById('videoActivity', query(ctx, 'id'))), { auth: true });
  add('POST', '/sys/videoActivity/addOrModify', async (ctx) => {
    await services.addOrModifyTimedActivity('videoActivity', body(ctx), { label: 'video', cacheKey: 'videoActivity' });
    return okStatus(true);
  }, { auth: true });
  add('DELETE', '/sys/videoActivity/delete', async (ctx) => {
    cache.del('videoActivity');
    return okStatus(await db.deleteByIds('videoActivity', ctx.body || []));
  }, { auth: true });
  add('POST', '/sys/videoActivity/setVideo', async (ctx) => {
    await services.setVideoActivityVideos(body(ctx));
    return ok();
  }, { auth: true });
  add('GET', '/sys/videoActivity/getVideo', async (ctx) => ok(await services.getVideoActivityVideos(query(ctx, 'id'))), { auth: true });

  add('POST', '/sys/viewNumSet/page', async () => ok(pageObject([{ viewNum: cache.get('userViewNum') }], 1, 1, 15)), { auth: true });
  add('POST', '/sys/viewNumSet/addOrModify', async (ctx) => {
    cache.set('userViewNum', body(ctx).viewNum);
    return ok();
  }, { auth: true });

  add('POST', '/sys/file-upload/upload', handleUpload, { auth: true });
  add('GET', '/sys/file-upload/cos/policy', async () => ok(cosPolicy()), { auth: true });
}

function registerWxRoutes() {
  add('GET', '/wx/agreement/getOneByType', async (ctx) => ok(await db.selectOne('agreement', [{ prop: 'type', value: query(ctx, 'type') }])));
  add('GET', '/wx/area/listByPid', async (ctx) => ok(await db.selectList('area', [{ prop: 'parentId', value: query(ctx, 'pid') }], { orderBy: 'area_id ASC' })));
  add('POST', '/wx/category/list', async () => ok(await db.selectList('category', [{ prop: 'status', value: true }], { orderBy: 'seq ASC' })));
  add('POST', '/wx/customServer/list', async (ctx) => ok(await db.selectList('customServer', db.filtersFromData('customServer', body(ctx)))));
  add('POST', '/wx/feedback/add', async (ctx) => okStatus(await services.wxFeedbackAdd(ctx.req, body(ctx))));
  add('GET', '/wx/feedback/page', async (ctx) => ok(await services.wxFeedbackPage(ctx.req, ctx.query)));
  add('GET', '/wx/feedback/getOneById', async (ctx) => ok(await db.getById('feedback', query(ctx, 'id'))));
  add('POST', '/wx/file-upload/upload', handleUpload);
  add('POST', '/wx/introduce/page', async (ctx) => ok(await services.introducePage(body(ctx))));
  add('GET', '/wx/introduce/getOneById', async (ctx) => ok(await db.getById('introduce', query(ctx, 'id'))));
  add('GET', '/wx/introduceSet/checkStatus', async () => {
    const setting = cache.get('introduceSet');
    const now = new Date();
    const active = !!setting && new Date(String(setting.startTime).replace(' ', 'T')) < now && new Date(String(setting.endTime).replace(' ', 'T')) > now;
    return ok(active);
  });
  add('POST', '/wx/login/wxAuth', async (ctx) => ok(await services.wxAuth(body(ctx))));
  add('GET', '/wx/login/logout', async (ctx) => {
    auth.logout(ctx.req);
    return ok();
  });
  add('POST', '/wx/payment/list', async (ctx) => ok(await db.selectList('payment', db.filtersFromData('payment', body(ctx)))));
  add('POST', '/wx/riskWarning/page', async (ctx) => ok(await services.wxRiskWarningPage(body(ctx))));
  add('GET', '/wx/riskWarning/getOneById', async (ctx) => ok(await services.getRiskWarning(ctx.req, query(ctx, 'id'))));
  add('GET', '/wx/riskWarning/readFinish', async (ctx) => ok(await services.readRiskWarning(ctx.req, query(ctx, 'id'))));
  add('GET', '/wx/signActivity/dailySign', async () => ok(null));
  add('GET', '/test/fix01', async () => ok(null));
  add('GET', '/wx/ticketActivity/getOpen', async (ctx) => ok(await services.getTicketActivityOpen(ctx.req)));
  add('GET', '/wx/ticketActivity/getOneById', async (ctx) => ok(await db.getById('ticketActivity', query(ctx, 'id'))));
  add('GET', '/wx/ticketActivity/surplusTicket', async (ctx) => ok(await services.surplusTicket(ctx.req)));
  add('GET', '/wx/topic/list', async (ctx) => ok(await services.listTopicsByLine(ctx.req, query(ctx, 'topicLineId'))));
  add('GET', '/wx/topic/listRandom', async (ctx) => ok(await services.listRandomTopics(ctx.req)));
  add('GET', '/wx/topicLine/list', async (ctx) => ok(await services.listTopicLines(ctx.req)));
  add('GET', '/wx/topicLine/checkContinue', async (ctx) => ok(await services.checkTopicContinue(ctx.req, query(ctx, 'id'))));
  add('POST', '/wx/topicRecordActivity/page', async (ctx) => ok(await services.topicRankPage(body(ctx))));
  add('GET', '/wx/topicRecordActivity/getRank', async (ctx) => ok(await services.getRank(ctx.req)));
  add('POST', '/wx/topicRecord/add', async (ctx) => {
    await services.addTopicRecord(ctx.req, body(ctx));
    return ok();
  });
  add('POST', '/wx/topicRecordSingle/add', async (ctx) => ok(await services.addSingleTopicRecord(ctx.req, body(ctx))));
  add('GET', '/wx/user/info', async (ctx) => ok(await services.userInfo(ctx.req)));
  add('POST', '/wx/user/update', async (ctx) => {
    await services.updateUser(ctx.req, body(ctx));
    return ok();
  });
  add('GET', '/wx/user/addViewNum', async () => {
    cache.increment('userViewNum', 1, 71939);
    return ok();
  });
  add('GET', '/wx/user/getViewNum', async () => ok(cache.get('userViewNum') || 1));
  add('GET', '/wx/userWallet/getOneByType', async (ctx) => ok(await services.getOrCreateWallet(auth.getLoginId(ctx.req), query(ctx, 'type', 1))));
  add('POST', '/wx/userWallet/scanPay', async (ctx) => {
    await services.scanPay(ctx.req, body(ctx));
    return ok();
  });
  add('GET', '/wx/userWallet/checkMax', async (ctx) => ok(await services.checkMax(ctx.req)));
  add('POST', '/wx/userWalletRecord/page', async (ctx) => {
    const input = body(ctx);
    const filters = [{ prop: 'walletId', value: input.walletId }];
    if (Number(input.inOut) === 1) {
      filters.push({ prop: 'changeAmount', op: 'gt', value: 0 });
    }
    if (Number(input.inOut) === 2) {
      filters.push({ prop: 'changeAmount', op: 'lt', value: 0 });
    }
    if (input.startTime) {
      filters.push({ prop: 'createTime', op: 'gte', value: input.startTime });
    }
    if (input.endTime) {
      filters.push({ prop: 'createTime', op: 'lte', value: input.endTime });
    }
    const page = await db.page('userWalletRecord', input, { filters, orderBy: 'id DESC' });
    page.records = page.records.map((record) => ({ ...record, eventTypeName: require('./models').walletEventLabels[record.eventType] || null }));
    return ok(page);
  });
  add('GET', '/wx/videoActivity/getOneById', async () => ok(await services.firstActive('videoActivity', 'videoActivity')));
  add('POST', '/wx/video/pageTicket', async (ctx) => ok(await services.pageTicket(ctx.req, body(ctx))));
  add('POST', '/wx/video/pageIntegral', async (ctx) => ok(await services.pageIntegral(ctx.req, body(ctx))));
  add('GET', '/wx/video/getOneById', async (ctx) => ok(await services.videoDetail(ctx.req, query(ctx, 'id'), query(ctx, 'activityType', 2))));
  add('GET', '/wx/video/doVote', async (ctx) => {
    await services.doVote(ctx.req, query(ctx, 'id'));
    return ok();
  });
  add('GET', '/wx/video/doReward', async (ctx) => {
    await services.doVideoReward(ctx.req, query(ctx, 'id'), query(ctx, 'activityType'));
    return ok();
  });
}

function registerSdkRoutes() {
  add('GET', '/sdk/get', async () => ok(await services.loadJsapiTicket(false)));
  add('GET', '/sdk/getJsapiTicket/forceRefresh', async (ctx) => ok(await services.loadJsapiTicket(query(ctx, 'forceRefresh') === 'true' || query(ctx, 'forceRefresh') === true)));
  add('POST', '/sdk/create', async (ctx) => ok(await services.createJsapiSignature(body(ctx), query(ctx, 'forceRefresh') === 'true' || query(ctx, 'forceRefresh') === true)));
}

registerAdminRoutes();
registerWxRoutes();
registerSdkRoutes();

async function handle(req, res) {
  setCors(res);
  if (req.method === 'OPTIONS') {
    res.statusCode = 204;
    res.end();
    return;
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  if (parsedUrl.pathname.startsWith('/uploads/') && await serveUpload(req, res, parsedUrl.pathname)) {
    return;
  }

  const route = matchRoute(req.method.toUpperCase(), parsedUrl.pathname);
  if (!route) {
    sendJson(res, result.failed(`接口不存在: ${req.method} ${parsedUrl.pathname}`, 404));
    return;
  }

  try {
    const ctx = {
      req,
      res,
      query: Object.fromEntries(parsedUrl.searchParams.entries()),
      body: await parseBody(req)
    };
    req.ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress || '';
    if (route.auth) {
      auth.getLoginId(req);
    }
    const payload = await route.handler(ctx);
    sendJson(res, payload && payload.code !== undefined ? payload : ok(payload));
  } catch (error) {
    if (error instanceof UnauthorizedError) {
      sendJson(res, result.unauthorized(error.message));
      return;
    }
    if (error instanceof YunKeError) {
      sendJson(res, result.failed(error.message, error.code));
      return;
    }
    sendJson(res, result.failed(error.message || '操作失败'));
  }
}

function createServer() {
  return http.createServer(handle);
}

module.exports = {
  createServer,
  routes
};
