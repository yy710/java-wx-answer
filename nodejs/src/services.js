const db = require('./db');
const cache = require('./cache');
const config = require('./config');
const auth = require('./auth');
const { WalletEvent, walletEventLabels } = require('./models');
const { YunKeError } = require('./errors');
const {
  buildTree,
  md5,
  normalizeAvatarUrl,
  nowString,
  pageObject,
  randomString,
  sha1,
  snowflakeId,
  todayString,
  toMoney,
  toNumber
} = require('./utils');

const INTEGRAL_TYPE = 1;
const INTEGRAL_MAX = 4000;

function fail(message) {
  throw new YunKeError(message);
}

function assertTrue(condition, message) {
  if (!condition) {
    fail(message);
  }
}

function dateValue(value) {
  if (!value) {
    return null;
  }
  return new Date(String(value).replace(' ', 'T'));
}

function addSecondsUntil(endTime) {
  const end = dateValue(endTime);
  if (!end) {
    return null;
  }
  return Math.max(Math.floor((end.getTime() - Date.now()) / 1000), 1);
}

async function firstActive(modelKey, cacheKey, conn) {
  const cached = cache.get(cacheKey);
  if (cached) {
    return cached;
  }
  const now = nowString();
  const rows = await db.raw(
    `SELECT * FROM ${db.quote(db.getModel(modelKey).table)} WHERE start_time <= ? AND end_time > ? ORDER BY start_time ASC LIMIT 1`,
    [now, now],
    conn
  );
  const item = rows[0] || null;
  if (item) {
    cache.set(cacheKey, item, addSecondsUntil(item.endTime));
  }
  return item;
}

function checkTimeLimit() {
  const now = new Date();
  const start = new Date('2026-05-21T00:01:00');
  const end = new Date('2026-05-31T23:59:00');
  if (now < start) {
    fail('获得积分开始时间为：2026-05-21 00:01:00');
  }
  if (now > end) {
    fail('获得积分结束时间为：2026-05-31 23:59:00');
  }
}

async function validateNoOverlap(modelKey, data, label) {
  const start = dateValue(data.startTime);
  const end = dateValue(data.endTime);
  assertTrue(start && end, '开始时间和结束时间不能为空');
  assertTrue(end >= start, '结束时间不能早于开始时间');
  assertTrue(end > new Date(), '结束时间不能早于当前时间');
  const params = [data.endTime, data.startTime];
  let sql = `SELECT id,title FROM ${db.quote(db.getModel(modelKey).table)} WHERE start_time <= ? AND end_time > ?`;
  if (data.id) {
    sql += ' AND id <> ?';
    params.push(data.id);
  }
  sql += ' LIMIT 1';
  const rows = await db.raw(sql, params);
  if (rows[0]) {
    fail(`时间与${label === 'video' ? '-' : '('}${rows[0].title}${label === 'video' ? '-' : ')'}时间重叠`);
  }
}

async function addOrModifyTimedActivity(modelKey, data, options = {}) {
  await validateNoOverlap(modelKey, data, options.label || '');
  if (modelKey === 'ticketActivity') {
    assertTrue(dateValue(data.startTicketTime) <= dateValue(data.endTicketTime), '投票开始时间不能晚于投票结束时间');
    assertTrue(dateValue(data.startTicketTime) >= dateValue(data.startTime), '投票开始时间不能早于开始时间');
    assertTrue(dateValue(data.endTicketTime) <= dateValue(data.endTime), '投票结束时间不能晚于结束时间');
  }
  const body = { ...data };
  if (!body.id) {
    if (modelKey === 'ticketActivity') {
      body.ticketUserNum = 0;
      body.ticketTotal = 0;
      body.viewNum = 0;
    }
    if (modelKey === 'topicActivity') {
      body.userNum = 0;
    }
  }
  await db.saveOrUpdate(modelKey, body);
  if (options.cacheKey) {
    cache.del(options.cacheKey);
  }
  return true;
}

async function genericJoinedPage(sql, countSql, params, body) {
  return db.pageRaw(sql, countSql, params, body.pageNum || 1, body.pageSize || 15);
}

async function feedbackPage(body) {
  const params = [];
  const where = [];
  if (body.nickName) {
    where.push('yu.nick_name LIKE ?');
    params.push(`%${body.nickName}%`);
  }
  if (body.problemType != null && body.problemType !== '') {
    where.push('yf.problem_type = ?');
    params.push(body.problemType);
  }
  if (body.handleFlag != null && body.handleFlag !== '') {
    where.push('yf.handle_flag = ?');
    params.push(body.handleFlag);
  }
  const whereSql = where.length ? ` WHERE ${where.join(' AND ')}` : '';
  return genericJoinedPage(
    `SELECT yf.*, yu.nick_name, yu.mobile FROM yk_feedback yf LEFT JOIN yk_user yu ON yu.id = yf.user_id${whereSql} ORDER BY yf.id DESC`,
    `SELECT COUNT(1) AS total FROM yk_feedback yf LEFT JOIN yk_user yu ON yu.id = yf.user_id${whereSql}`,
    params,
    body
  );
}

async function riskWarningPage(body) {
  const params = [];
  const where = [];
  if (body.title) {
    where.push('rw.title LIKE ?');
    params.push(`%${body.title}%`);
  }
  const whereSql = where.length ? ` WHERE ${where.join(' AND ')}` : '';
  return genericJoinedPage(
    `SELECT rw.*, c.title AS category_title FROM risk_warning rw LEFT JOIN category c ON rw.category_id = c.id${whereSql} ORDER BY rw.seq ASC`,
    `SELECT COUNT(1) AS total FROM risk_warning rw LEFT JOIN category c ON rw.category_id = c.id${whereSql}`,
    params,
    body
  );
}

async function riskWarningRecordPage(body) {
  const params = [body.riskWarnId];
  let extra = '';
  if (body.nickName) {
    extra = ' AND yu.nick_name LIKE ?';
    params.push(`%${body.nickName}%`);
  }
  return genericJoinedPage(
    `SELECT rwr.*, yu.nick_name FROM risk_warning_record rwr LEFT JOIN yk_user yu ON rwr.user_id = yu.id WHERE rwr.risk_warn_id = ?${extra} ORDER BY rwr.create_time DESC`,
    `SELECT COUNT(1) AS total FROM risk_warning_record rwr LEFT JOIN yk_user yu ON rwr.user_id = yu.id WHERE rwr.risk_warn_id = ?${extra}`,
    params,
    body
  );
}

async function topicRecordPage(body) {
  const params = [];
  const where = [];
  if (body.topicActivityTitle) {
    where.push('tr.topic_activity_title LIKE ?');
    params.push(`%${body.topicActivityTitle}%`);
  }
  if (body.topicLineTitle) {
    where.push('tr.topic_line_title LIKE ?');
    params.push(`%${body.topicLineTitle}%`);
  }
  if (body.nickName) {
    where.push('yu.nick_name LIKE ?');
    params.push(`%${body.nickName}%`);
  }
  const whereSql = where.length ? ` WHERE ${where.join(' AND ')}` : '';
  return genericJoinedPage(
    `SELECT tr.*, yu.nick_name FROM topic_record tr LEFT JOIN yk_user yu ON yu.id = tr.user_id${whereSql} ORDER BY tr.id DESC`,
    `SELECT COUNT(1) AS total FROM topic_record tr LEFT JOIN yk_user yu ON yu.id = tr.user_id${whereSql}`,
    params,
    body
  );
}

async function topicRecordSinglePage(body) {
  const params = [];
  const where = [];
  if (body.nickName) {
    where.push('yu.nick_name LIKE ?');
    params.push(`%${body.nickName}%`);
  }
  if (body.topicTitle) {
    where.push('trs.topic_title LIKE ?');
    params.push(`%${body.topicTitle}%`);
  }
  const whereSql = where.length ? ` WHERE ${where.join(' AND ')}` : '';
  return genericJoinedPage(
    `SELECT trs.*, yu.nick_name FROM topic_record_single trs LEFT JOIN yk_user yu ON trs.user_id = yu.id${whereSql} ORDER BY trs.id DESC`,
    `SELECT COUNT(1) AS total FROM topic_record_single trs LEFT JOIN yk_user yu ON trs.user_id = yu.id${whereSql}`,
    params,
    body
  );
}

async function ticketRecordPage(body) {
  const params = [];
  const where = [];
  if (body.ticketActivityId) {
    where.push('tr.ticket_activity_id = ?');
    params.push(body.ticketActivityId);
  }
  if (body.ticketVideoId) {
    where.push('tr.ticket_video_id = ?');
    params.push(body.ticketVideoId);
  }
  const whereSql = where.length ? ` WHERE ${where.join(' AND ')}` : '';
  return genericJoinedPage(
    `SELECT tr.*, yu.nick_name FROM ticket_record tr LEFT JOIN yk_user yu ON yu.id = tr.user_id${whereSql} ORDER BY tr.id DESC`,
    `SELECT COUNT(1) AS total FROM ticket_record tr LEFT JOIN yk_user yu ON yu.id = tr.user_id${whereSql}`,
    params,
    body
  );
}

async function userWalletPage(body) {
  const params = [];
  const where = [];
  if (body.nickName) {
    where.push('yu.nick_name LIKE ?');
    params.push(`%${body.nickName}%`);
  }
  const whereSql = where.length ? ` WHERE ${where.join(' AND ')}` : '';
  return genericJoinedPage(
    `SELECT uw.*, yu.nick_name FROM user_wallet uw LEFT JOIN yk_user yu ON uw.user_id = yu.id${whereSql} ORDER BY uw.amount DESC`,
    `SELECT COUNT(1) AS total FROM user_wallet uw LEFT JOIN yk_user yu ON uw.user_id = yu.id${whereSql}`,
    params,
    body
  );
}

async function userWalletRecordPage(body) {
  const params = [];
  const where = [];
  if (body.nickName) {
    where.push('yu.nick_name LIKE ?');
    params.push(`%${body.nickName}%`);
  }
  if (body.walletId) {
    where.push('uwr.wallet_id = ?');
    params.push(body.walletId);
  }
  if (body.userId) {
    where.push('uw.user_id = ?');
    params.push(body.userId);
  }
  const whereSql = where.length ? ` WHERE ${where.join(' AND ')}` : '';
  const page = await genericJoinedPage(
    `SELECT uwr.*, yu.nick_name FROM user_wallet_record uwr LEFT JOIN user_wallet uw ON uw.id = uwr.wallet_id LEFT JOIN yk_user yu ON uw.user_id = yu.id${whereSql} ORDER BY uwr.id DESC`,
    `SELECT COUNT(1) AS total FROM user_wallet_record uwr LEFT JOIN user_wallet uw ON uw.id = uwr.wallet_id LEFT JOIN yk_user yu ON uw.user_id = yu.id${whereSql}`,
    params,
    body
  );
  page.records = page.records.map((record) => ({
    ...record,
    eventTypeName: walletEventLabels[record.eventType] || null,
    beforeAmount: toMoney(record.afterAmount) - toMoney(record.changeAmount)
  }));
  return page;
}

async function sysUserPage(body) {
  const pageNum = toNumber(body.pageNum, 1);
  const pageSize = toNumber(body.pageSize, 15);
  const params = [];
  let where = '';
  if (body.username) {
    where = ' WHERE username LIKE ?';
    params.push(`%${body.username}%`);
  }
  const totalRows = await db.raw(`SELECT COUNT(1) AS total FROM sys_user${where}`, params);
  const rows = await db.raw(
    `SELECT id, username, icon, create_time, nick_name, update_time, status FROM sys_user${where} ORDER BY id DESC LIMIT ? OFFSET ?`,
    [...params, pageSize, (pageNum - 1) * pageSize]
  );
  const ids = rows.map((row) => row.id);
  const roles = ids.length
    ? await db.raw(
      `SELECT sur.user_id, sr.id, sr.name, sr.code FROM sys_user_role sur LEFT JOIN sys_role sr ON sr.id = sur.role_id WHERE sur.user_id IN (${ids.map(() => '?').join(',')})`,
      ids
    )
    : [];
  const byUser = new Map();
  for (const role of roles) {
    if (!byUser.has(String(role.userId))) {
      byUser.set(String(role.userId), []);
    }
    if (role.id) {
      byUser.get(String(role.userId)).push({ id: role.id, name: role.name, code: role.code });
    }
  }
  return pageObject(rows.map((row) => ({ ...row, roleVo: byUser.get(String(row.id)) || [] })), toNumber(totalRows[0] && totalRows[0].total), pageNum, pageSize);
}

async function sysLogin(body) {
  const user = await db.selectOne('sysUser', [{ prop: 'username', value: body.username }]);
  assertTrue(user, '账号不存在');
  assertTrue(md5(body.password) === user.password, '密码错误');
  const token = auth.createToken(user.id, 'sys');
  const userInfo = { ...user, password: null };
  return { userInfo, token: { tokenName: token.tokenName, tokenValue: token.tokenValue } };
}

async function addSysUser(body) {
  const exists = await db.selectOne('sysUser', [{ prop: 'username', value: body.username }]);
  assertTrue(!exists, '该账号已存在');
  await db.insert('sysUser', { ...body, password: md5(body.password) });
}

async function modifySysUser(body) {
  const exists = await db.raw('SELECT id FROM sys_user WHERE username = ? AND id <> ? LIMIT 1', [body.username, body.id]);
  assertTrue(!exists[0], '该账号已存在');
  const copy = { ...body };
  delete copy.password;
  await db.updateById('sysUser', body.id, copy);
}

async function deleteSysUsers(ids) {
  await db.transaction(async (conn) => {
    await db.deleteByIds('sysUser', ids, conn);
    await db.deleteWhere('sysUserRole', [{ prop: 'userId', op: 'in', value: ids }], conn);
  });
}

async function setUserRole(body) {
  const exists = await db.selectOne('sysUserRole', [
    { prop: 'userId', value: body.userId },
    { prop: 'roleId', value: body.roleId }
  ]);
  if (!exists) {
    await db.insert('sysUserRole', body);
  }
}

async function deleteUserRole(body) {
  await db.deleteWhere('sysUserRole', [
    { prop: 'userId', value: body.userId },
    { prop: 'roleId', value: body.roleId }
  ]);
}

async function deleteSysRoles(ids) {
  await db.transaction(async (conn) => {
    await db.deleteByIds('sysRole', ids, conn);
    await db.deleteWhere('sysRolePermission', [{ prop: 'roleId', op: 'in', value: ids }], conn);
  });
}

async function setRolePermission(body) {
  await db.transaction(async (conn) => {
    await db.deleteWhere('sysRolePermission', [{ prop: 'roleId', value: body.id }], conn);
    for (const permissionId of body.permissionIds || []) {
      await db.insert('sysRolePermission', { roleId: body.id, permissionId }, conn);
    }
  });
}

async function listPermissionByRole(roleId) {
  return db.raw(
    'SELECT sp.* FROM sys_permission sp LEFT JOIN sys_role_permission srp ON srp.permission_id = sp.id WHERE srp.role_id = ? AND sp.status = 1',
    [roleId]
  );
}

async function listPermissionByUser(userId) {
  if (String(userId) === '1') {
    return db.selectList('sysPermission', [{ prop: 'status', value: 1 }], { orderBy: 'sort ASC' });
  }
  return db.raw(
    'SELECT DISTINCT sp.* FROM sys_permission sp LEFT JOIN sys_role_permission srp ON srp.permission_id = sp.id LEFT JOIN sys_user_role sur ON sur.role_id = srp.role_id WHERE sur.user_id = ? AND sp.status = 1 ORDER BY sp.sort ASC',
    [userId]
  );
}

async function permissionTree(userId) {
  const list = userId
    ? await listPermissionByUser(userId)
    : await db.selectList('sysPermission', [{ prop: 'status', value: 1 }], { orderBy: 'sort ASC' });
  return buildTree(list, '0', 'pid', 'id');
}

async function permissionTreeByPid(pid) {
  const list = await db.selectList('sysPermission', [], { orderBy: 'sort ASC' });
  return buildTree(list, pid || '0', 'pid', 'id');
}

async function deletePermission(id) {
  const children = await db.selectList('sysPermission', [{ prop: 'pid', value: id }]);
  for (const child of children) {
    await deletePermission(child.id);
  }
  await db.deleteByIds('sysPermission', [id]);
  await db.deleteWhere('sysRolePermission', [{ prop: 'permissionId', value: id }]);
}

async function areaTree() {
  const cached = cache.get('areaTree');
  if (cached) {
    return cached;
  }
  const list = await db.selectList('area', [], { orderBy: 'area_id ASC' });
  const attach = (parentId) => list
    .filter((item) => String(item.parentId || '') === String(parentId))
    .map((item) => ({ ...item, children: attach(item.areaId) }));
  const result = list.filter((item) => Number(item.level) === 1).map((item) => ({ ...item, children: attach(item.areaId) }));
  cache.set('areaTree', result, 7 * 24 * 3600);
  return result;
}

async function topicWithItems(id) {
  const topic = await db.getById('topic', id);
  if (topic) {
    topic.topicItemList = await db.selectList('topicItem', [{ prop: 'topicId', value: id }], { orderBy: 'seq ASC' });
  }
  return topic;
}

async function saveTopic(body) {
  await db.transaction(async (conn) => {
    let id = body.id;
    if (id) {
      await db.updateById('topic', id, body, conn);
      await db.deleteWhere('topicItem', [{ prop: 'topicId', value: id }], conn);
    } else {
      const inserted = await db.insert('topic', body, conn);
      id = inserted.id;
    }
    for (const item of body.topicItemList || []) {
      await db.insert('topicItem', { ...item, id: snowflakeId(), topicId: id }, conn);
    }
  });
}

async function deleteTopics(ids) {
  await db.transaction(async (conn) => {
    await db.deleteByIds('topic', ids, conn);
    await db.deleteWhere('topicItem', [{ prop: 'topicId', op: 'in', value: ids }], conn);
  });
}

async function topicRecordDetail(id) {
  const record = await db.getById('topicRecord', id);
  if (!record) {
    return null;
  }
  const user = await db.getById('user', record.userId);
  record.nickName = user ? user.nickName : null;
  const topics = await db.selectList('topicRecordTopic', [{ prop: 'topicRecordId', value: id }]);
  for (const item of topics) {
    item.topicRecordTopicItemList = await db.selectList('topicRecordTopicItem', [{ prop: 'topicRecordTopicId', value: item.id }]);
  }
  record.topicRecordTopicList = topics;
  return record;
}

async function singleRecordDetail(id) {
  const record = await db.getById('topicRecordSingle', id);
  if (!record) {
    return null;
  }
  const user = await db.getById('user', record.userId);
  record.nickName = user ? user.nickName : null;
  record.singleItemList = await db.selectList('topicRecordSingleItem', [{ prop: 'topicRecordSingleId', value: id }]);
  return record;
}

async function setTicketVideos(body) {
  await db.transaction(async (conn) => {
    await db.deleteWhere('ticketActivityVideo', [{ prop: 'ticketActivityId', value: body.id }], conn);
    for (const item of body.videoList || []) {
      await db.insert('ticketActivityVideo', { ...item, ticketActivityId: body.id }, conn);
    }
  });
}

async function getTicketVideos(id) {
  const list = await db.selectList('ticketActivityVideo', [{ prop: 'ticketActivityId', value: id }], { orderBy: 'seq ASC' });
  for (const item of list) {
    const video = await db.getById('video', item.videoId);
    if (video) {
      item.videoTitle = video.title;
      item.videoUrlPic = video.urlPic;
      item.videoUrlVideo = video.urlVideo;
    }
  }
  return list;
}

async function setVideoActivityVideos(body) {
  await db.transaction(async (conn) => {
    await db.deleteWhere('videoActivityVideo', [{ prop: 'videoActivityId', value: body.id }], conn);
    for (const item of body.videoList || []) {
      await db.insert('videoActivityVideo', { ...item, videoActivityId: body.id }, conn);
    }
  });
}

async function getVideoActivityVideos(id) {
  const list = await db.selectList('videoActivityVideo', [{ prop: 'videoActivityId', value: id }], { orderBy: 'seq ASC' });
  for (const item of list) {
    const video = await db.getById('video', item.videoId);
    if (video) {
      item.videoTitle = video.title;
      item.videoUrlPic = video.urlPic;
      item.videoUrlVideo = video.urlVideo;
    }
  }
  return list;
}

async function dataAnalysis() {
  const cached = cache.get('indexAnalysis');
  if (cached) {
    return cached;
  }
  const today = todayString();
  const totalUserNum = await db.count('user');
  const todayUserRows = await db.raw('SELECT COUNT(1) AS total FROM yk_user WHERE DATE(create_time) = ?', [today]);
  const totalTicketNum = await db.count('ticketRecord');
  const todayTicketRows = await db.raw('SELECT COUNT(1) AS total FROM ticket_record WHERE DATE(create_time) = ?', [today]);
  const todayIntegralUserRows = await db.raw('SELECT COUNT(DISTINCT wallet_id) AS total FROM user_wallet_record WHERE DATE(create_time) = ?', [today]);
  const todayIntegralRows = await db.raw('SELECT SUM(change_amount) AS total FROM user_wallet_record WHERE change_amount > 0 AND DATE(create_time) = ?', [today]);
  const totalIntegralUserRows = await db.raw('SELECT COUNT(DISTINCT wallet_id) AS total FROM user_wallet_record');
  const totalIntegralRows = await db.raw('SELECT SUM(change_amount) AS total FROM user_wallet_record WHERE change_amount > 0');
  const data = {
    totalUserNum,
    todayUserNum: toNumber(todayUserRows[0] && todayUserRows[0].total),
    totalTicketNum,
    todayTicketNum: toNumber(todayTicketRows[0] && todayTicketRows[0].total),
    todayIntegralUserNum: toNumber(todayIntegralUserRows[0] && todayIntegralUserRows[0].total),
    todayIntegralAmount: toMoney(todayIntegralRows[0] && todayIntegralRows[0].total),
    totalIntegralUserNum: toNumber(totalIntegralUserRows[0] && totalIntegralUserRows[0].total),
    totalIntegralAmount: toMoney(totalIntegralRows[0] && totalIntegralRows[0].total)
  };
  cache.set('indexAnalysis', data, 600);
  return data;
}

async function wxAuth(body) {
  let openId = '123456';
  let wxUserInfo = null;
  if (body.code) {
    assertTrue(config.wx.appId && config.wx.secret, '微信公众号 appId 或 secret 不能为空');
    const tokenUrl = `https://api.weixin.qq.com/sns/oauth2/access_token?appid=${encodeURIComponent(config.wx.appId)}&secret=${encodeURIComponent(config.wx.secret)}&code=${encodeURIComponent(body.code)}&grant_type=authorization_code`;
    const tokenRes = await fetch(tokenUrl);
    const tokenJson = await tokenRes.json();
    if (tokenJson.errcode) {
      fail('微信授权异常-请刷新重试');
    }
    openId = tokenJson.openid;
    if (tokenJson.access_token) {
      const infoUrl = `https://api.weixin.qq.com/sns/userinfo?access_token=${encodeURIComponent(tokenJson.access_token)}&openid=${encodeURIComponent(openId)}&lang=zh_CN`;
      const infoRes = await fetch(infoUrl);
      const infoJson = await infoRes.json();
      if (!infoJson.errcode) {
        wxUserInfo = infoJson;
      }
    }
  }
  let user = await db.selectOne('user', [{ prop: 'openId', value: openId }]);
  if (!user) {
    const nickName = wxUserInfo && wxUserInfo.nickname ? wxUserInfo.nickname : `用户${randomString(6)}`;
    user = await db.insert('user', {
      openId,
      nickName,
      pic: normalizeAvatarUrl(wxUserInfo && wxUserInfo.headimgurl),
      parentId: body.parentId || null,
      createTime: nowString()
    });
    if (body.parentId) {
      await rewardInviteParent(body.parentId, user.id);
    }
  } else {
    const update = {};
    if (wxUserInfo && wxUserInfo.nickname && !user.nickName) {
      update.nickName = wxUserInfo.nickname;
    }
    if (wxUserInfo && wxUserInfo.headimgurl && !user.pic) {
      update.pic = normalizeAvatarUrl(wxUserInfo.headimgurl);
    }
    if (Object.keys(update).length) {
      await db.updateById('user', user.id, update);
      user = { ...user, ...update };
    }
    user.pic = normalizeAvatarUrl(user.pic);
  }
  const token = auth.createToken(user.id, 'wx');
  token.tag = '0000';
  return { userInfo: user, token };
}

async function rewardInviteParent(parentId, newUserId) {
  const parent = await db.getById('user', parentId);
  if (!parent) {
    return;
  }
  const inviteSet = await db.selectOne('inviteSet', [], { orderBy: 'id ASC' });
  if (!inviteSet || toMoney(inviteSet.rewardAmount) <= 0) {
    return;
  }
  const wallet = await getOrCreateWallet(parentId, INTEGRAL_TYPE);
  const countRows = await db.raw('SELECT COUNT(1) AS total FROM user_wallet_record WHERE wallet_id = ? AND event_type = ?', [wallet.id, WalletEvent.INVITE_REWARD]);
  if (toNumber(countRows[0] && countRows[0].total) >= toNumber(inviteSet.rewardLimit)) {
    return;
  }
  await rewardIntegral(parentId, newUserId, WalletEvent.INVITE_REWARD, inviteSet.rewardAmount);
}

async function getOrCreateWallet(userId, type = INTEGRAL_TYPE, conn) {
  let wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: type }], {}, conn);
  if (wallet) {
    return wallet;
  }
  wallet = await db.insert('userWallet', { userId, type, amount: 0, version: 0 }, conn);
  return wallet;
}

async function insertWalletRecord(walletId, eventId, eventType, changeAmount, afterAmount, conn) {
  return db.insert('userWalletRecord', {
    walletId,
    eventId,
    eventType,
    changeAmount,
    afterAmount,
    status: true,
    createTime: nowString()
  }, conn);
}

async function rewardIntegral(userId, eventId, eventType, rewardAmount, conn) {
  const amount = toMoney(rewardAmount);
  assertTrue(amount > 0, '奖励积分无效');
  const run = async (innerConn) => {
    const wallet = await getOrCreateWallet(userId, INTEGRAL_TYPE, innerConn);
    if (toMoney(wallet.amount) >= INTEGRAL_MAX) {
      fail('已达积分上限');
    }
    const afterAmount = toMoney(wallet.amount) + amount;
    await db.updateById('userWallet', wallet.id, { amount: afterAmount, version: toNumber(wallet.version) + 1 }, innerConn);
    await insertWalletRecord(wallet.id, eventId, eventType, amount, afterAmount, innerConn);
    return { ...wallet, amount: afterAmount, version: toNumber(wallet.version) + 1 };
  };
  return conn ? run(conn) : db.transaction(run);
}

async function userInfo(req) {
  const user = await db.getById('user', auth.getLoginId(req));
  return user ? { ...user, pic: normalizeAvatarUrl(user.pic) } : null;
}

async function updateUser(req, body) {
  const id = auth.getLoginId(req);
  await db.updateById('user', id, { id, pic: body.pic, nickName: body.nickName });
}

async function scanPay(req, body) {
  const userId = auth.getLoginId(req);
  const payment = await db.getById('payment', body.paymentId);
  assertTrue(payment && Number(payment.status) === 1, '收款方不存在');
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  assertTrue(wallet && toMoney(wallet.amount) >= toMoney(body.amount), '剩余积分不足');
  const afterAmount = toMoney(wallet.amount) - toMoney(body.amount);
  await db.transaction(async (conn) => {
    await db.updateById('userWallet', wallet.id, { amount: afterAmount, version: toNumber(wallet.version) + 1 }, conn);
    await insertWalletRecord(wallet.id, payment.id, WalletEvent.SCAN_PAY, -toMoney(body.amount), afterAmount, conn);
  });
}

async function checkMax(req) {
  const userId = auth.getLoginId(req);
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  return !!wallet && toMoney(wallet.amount) >= INTEGRAL_MAX;
}

async function wxFeedbackAdd(req, body) {
  await db.insert('feedback', { ...body, userId: auth.getLoginId(req), handleFlag: false });
  return true;
}

async function wxFeedbackPage(req, query) {
  return db.page('feedback', { ...query, userId: auth.getLoginId(req) }, { orderBy: 'create_time DESC' });
}

async function wxRiskWarningPage(body) {
  const filters = [{ prop: 'status', value: true }];
  if (body.categoryId) {
    filters.push({ prop: 'categoryId', value: body.categoryId });
  }
  return db.page('riskWarning', body, { filters, orderBy: 'seq ASC' });
}

async function getRiskWarning(req, id) {
  const userId = auth.getLoginId(req);
  const warning = await db.getById('riskWarning', id);
  if (!warning) {
    return null;
  }
  const record = await db.selectOne('riskWarningRecord', [{ prop: 'riskWarnId', value: id }, { prop: 'userId', value: userId }]);
  if (record) {
    warning.readFlag = true;
    const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
    if (wallet) {
      const reward = await db.selectOne('userWalletRecord', [
        { prop: 'walletId', value: wallet.id },
        { prop: 'eventId', value: id },
        { prop: 'eventType', value: WalletEvent.RISK_READ }
      ]);
      warning.rewardFlag = !!reward;
    }
  } else {
    await db.insert('riskWarningRecord', { riskWarnId: id, userId, createTime: nowString() });
  }
  await db.updateById('riskWarning', id, { viewNum: toNumber(warning.viewNum) + 1 });
  return warning;
}

async function readRiskWarning(req, id) {
  checkTimeLimit();
  const userId = auth.getLoginId(req);
  const warning = await db.getById('riskWarning', id);
  assertTrue(warning && Number(warning.status) === 1 && toMoney(warning.rewardAmount) > 0, '风险提示不存在或已删除或未设置积分奖励');
  const rewardSet = await db.selectOne('rewardSet', [{ prop: 'type', value: 1 }]);
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  if (rewardSet && Number(rewardSet.firstFlag) === 1) {
    const firstRecord = await db.selectOne('riskWarningRecord', [{ prop: 'riskWarnId', value: id }, { prop: 'userId', value: userId }], { orderBy: 'create_time ASC' });
    if (firstRecord && String(firstRecord.createTime).slice(0, 10) < todayString()) {
      fail(`首次阅读才可获得积分，当前首次阅读时间为：${firstRecord.createTime}`);
    }
  }
  if (wallet) {
    if (toMoney(wallet.amount) >= INTEGRAL_MAX) {
      fail('已达积分上限');
    }
    if (rewardSet) {
      const todayRows = await db.raw('SELECT COUNT(1) AS total FROM user_wallet_record WHERE wallet_id = ? AND event_type = ? AND DATE(create_time) = ?', [wallet.id, WalletEvent.RISK_READ, todayString()]);
      if (toNumber(todayRows[0] && todayRows[0].total) >= toNumber(rewardSet.rewardLimit)) {
        fail(`今日已达到次数上限${rewardSet.rewardLimit}`);
      }
    }
    const rewarded = await db.selectOne('userWalletRecord', [
      { prop: 'walletId', value: wallet.id },
      { prop: 'eventId', value: id },
      { prop: 'eventType', value: WalletEvent.RISK_READ }
    ]);
    if (rewarded) {
      return 0;
    }
  }
  await rewardIntegral(userId, id, WalletEvent.RISK_READ, warning.rewardAmount);
  await db.updateById('riskWarning', id, { rewardNum: toNumber(warning.rewardNum) + 1 });
  return toMoney(warning.rewardAmount);
}

async function getTicketActivityOpen(req) {
  const activity = await firstActive('ticketActivity', 'ticketActivity');
  if (!activity) {
    return null;
  }
  const now = new Date();
  if (dateValue(activity.endTicketTime) && now > dateValue(activity.endTicketTime)) {
    const userId = auth.getLoginId(req);
    const rows = await db.raw('SELECT COUNT(1) AS total FROM ticket_record WHERE ticket_activity_id = ? AND user_id = ?', [activity.id, userId]);
    return {
      ...activity,
      myTicketNum: toNumber(rows[0] && rows[0].total),
      ticketTotal: toNumber(activity.ticketTotal) * toNumber(activity.ticketMultiple, 1),
      ticketUserNum: toNumber(activity.ticketUserNum) * toNumber(activity.ticketUserMultiple, 1),
      viewNum: toNumber(activity.viewNum) * toNumber(activity.viewMultiple, 1),
      finishFlag: true
    };
  }
  return activity;
}

async function surplusTicket(req) {
  const activity = await firstActive('ticketActivity', 'ticketActivity');
  if (!activity) {
    return 0;
  }
  const userId = auth.getLoginId(req);
  const rows = await db.raw('SELECT COUNT(1) AS total FROM ticket_record WHERE ticket_activity_id = ? AND user_id = ? AND DATE(create_time) = ?', [activity.id, userId, todayString()]);
  return toNumber(activity.ticketLimit) - toNumber(rows[0] && rows[0].total);
}

async function pageTicket(req, body) {
  const pageNum = toNumber(body.pageNum, 1);
  const pageSize = toNumber(body.pageSize, 15);
  const activity = await firstActive('ticketActivity', 'ticketActivity');
  if (!activity) {
    return pageObject([], 0, pageNum, pageSize);
  }
  const page = await db.pageRaw(
    `SELECT tav.reward_amount, tav.min_time, v.* FROM ticket_activity_video tav LEFT JOIN video v ON v.id = tav.video_id WHERE tav.ticket_activity_id = ? AND v.status = 1 ORDER BY tav.seq`,
    `SELECT COUNT(1) AS total FROM ticket_activity_video tav LEFT JOIN video v ON v.id = tav.video_id WHERE tav.ticket_activity_id = ? AND v.status = 1`,
    [activity.id],
    pageNum,
    pageSize
  );
  const userId = auth.maybeLoginId(req);
  if (!userId) {
    return page;
  }
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  for (const video of page.records) {
    if (wallet && toMoney(video.rewardAmount) > 0) {
      const reward = await db.selectOne('userWalletRecord', [
        { prop: 'walletId', value: wallet.id },
        { prop: 'eventId', value: video.id },
        { prop: 'eventType', value: WalletEvent.TICKET_VIDEO }
      ]);
      video.getFlag = !!reward;
    }
    const countRows = await db.raw('SELECT COUNT(1) AS total FROM ticket_record WHERE ticket_video_id = ? AND user_id = ?', [video.id, userId]);
    video.ticketNum = toNumber(countRows[0] && countRows[0].total);
    video.ticketTotal = toNumber(video.ticketTotal) * toNumber(activity.ticketMultiple, 1);
  }
  return page;
}

async function pageIntegral(req, body) {
  const pageNum = toNumber(body.pageNum, 1);
  const pageSize = toNumber(body.pageSize, 15);
  const activity = await firstActive('videoActivity', 'videoActivity');
  if (!activity) {
    return pageObject([], 0, pageNum, pageSize);
  }
  const page = await db.pageRaw(
    `SELECT v.*, vav.min_time, vav.reward_amount FROM video_activity_video vav LEFT JOIN video v ON v.id = vav.video_id WHERE vav.video_activity_id = ? AND v.status = 1 ORDER BY vav.seq ASC`,
    `SELECT COUNT(1) AS total FROM video_activity_video vav LEFT JOIN video v ON v.id = vav.video_id WHERE vav.video_activity_id = ? AND v.status = 1`,
    [activity.id],
    pageNum,
    pageSize
  );
  const userId = auth.getLoginId(req);
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  if (wallet) {
    for (const video of page.records) {
      const record = await db.selectOne('userWalletRecord', [
        { prop: 'walletId', value: wallet.id },
        { prop: 'eventId', value: video.id },
        { prop: 'eventType', value: WalletEvent.VIDEO }
      ]);
      video.getFlag = !!record;
    }
  }
  return page;
}

async function doVote(req, id) {
  const userId = auth.getLoginId(req);
  const video = await db.getById('video', id);
  assertTrue(video, '视频不存在或已下架-请刷新页面^_^');
  const activity = await firstActive('ticketActivity', 'ticketActivity');
  assertTrue(activity, '无进行中的投票活动-请刷新页面^_^');
  const now = new Date();
  if (dateValue(activity.startTicketTime) > now) {
    fail(`投票未开始,最早投票时间：${activity.startTicketTime}`);
  }
  if (dateValue(activity.endTicketTime) < now) {
    fail('投票时间已截止');
  }
  const link = await db.selectOne('ticketActivityVideo', [{ prop: 'ticketActivityId', value: activity.id }, { prop: 'videoId', value: id }]);
  assertTrue(link, '当前视频不可投票-请刷新页面重试');
  const usedRows = await db.raw('SELECT COUNT(1) AS total FROM ticket_record WHERE ticket_activity_id = ? AND user_id = ? AND DATE(create_time) = ?', [activity.id, userId, todayString()]);
  assertTrue(toNumber(usedRows[0] && usedRows[0].total) < toNumber(activity.ticketLimit), '当日票数已用完');
  await db.transaction(async (conn) => {
    await db.insert('ticketRecord', { userId, ticketActivityId: activity.id, ticketVideoId: id, ipAddr: req.ip || '', createTime: nowString() }, conn);
    await db.updateById('video', id, { ticketTotal: toNumber(video.ticketTotal) + 1 }, conn);
    const userRows = await db.raw('SELECT COUNT(DISTINCT user_id) AS total FROM ticket_record WHERE ticket_activity_id = ?', [activity.id], conn);
    await db.updateById('ticketActivity', activity.id, {
      ticketTotal: toNumber(activity.ticketTotal) + 1,
      ticketUserNum: toNumber(userRows[0] && userRows[0].total)
    }, conn);
  });
}

async function doVideoReward(req, id, activityType) {
  checkTimeLimit();
  const userId = auth.getLoginId(req);
  const video = await db.getById('video', id);
  assertTrue(video && Number(video.status) === 1, '视频不存在或已下架-请刷新页面重试');
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  let eventType;
  let rewardAmount;
  if (Number(activityType) === 1) {
    const activity = await firstActive('videoActivity', 'videoActivity');
    assertTrue(activity, '无进行中的视频奖励积分活动-请刷新页面重试');
    const link = await db.selectOne('videoActivityVideo', [{ prop: 'videoActivityId', value: activity.id }, { prop: 'videoId', value: id }]);
    assertTrue(link && toMoney(link.rewardAmount) > 0, '当前视频不可获得积分');
    if (wallet) {
      assertTrue(toMoney(wallet.amount) < INTEGRAL_MAX, '已达积分上限');
      const existing = await db.selectOne('userWalletRecord', [{ prop: 'walletId', value: wallet.id }, { prop: 'eventType', value: WalletEvent.VIDEO }, { prop: 'eventId', value: id }]);
      assertTrue(!existing, '已获得当前视频的积分');
      if (activity.rewardLimit != null) {
        const rewardRows = await db.raw('SELECT COUNT(1) AS total FROM user_wallet_record WHERE wallet_id = ? AND event_type = ? AND DATE(create_time) = ?', [wallet.id, WalletEvent.VIDEO, todayString()]);
        assertTrue(toNumber(rewardRows[0] && rewardRows[0].total) < toNumber(activity.rewardLimit), `已达每日获得奖励次数上限:${activity.rewardLimit}`);
      }
    }
    eventType = WalletEvent.VIDEO;
    rewardAmount = link.rewardAmount;
  } else {
    const activity = await firstActive('ticketActivity', 'ticketActivity');
    assertTrue(activity, '无进行中的投票活动-请刷新页面重试');
    const link = await db.selectOne('ticketActivityVideo', [{ prop: 'ticketActivityId', value: activity.id }, { prop: 'videoId', value: id }]);
    assertTrue(link && toMoney(link.rewardAmount) > 0, '当前视频不可获得积分');
    if (wallet) {
      const existing = await db.selectOne('userWalletRecord', [{ prop: 'walletId', value: wallet.id }, { prop: 'eventType', value: WalletEvent.TICKET_VIDEO }, { prop: 'eventId', value: id }]);
      assertTrue(!existing, '已获得当前视频的积分');
    }
    eventType = WalletEvent.TICKET_VIDEO;
    rewardAmount = link.rewardAmount;
  }
  await rewardIntegral(userId, id, eventType, rewardAmount);
}

async function videoDetail(req, id, activityType = 2) {
  const video = await db.getById('video', id);
  if (!video) {
    return null;
  }
  if (Number(activityType) === 2) {
    const activity = await firstActive('ticketActivity', 'ticketActivity');
    if (activity) {
      const link = await db.selectOne('ticketActivityVideo', [{ prop: 'ticketActivityId', value: activity.id }, { prop: 'videoId', value: id }]);
      if (link) {
        video.minTime = link.minTime;
        video.rewardAmount = link.rewardAmount;
      }
    }
  } else {
    const activity = await firstActive('videoActivity', 'videoActivity');
    if (activity) {
      const link = await db.selectOne('videoActivityVideo', [{ prop: 'videoActivityId', value: activity.id }, { prop: 'videoId', value: id }]);
      if (link) {
        video.minTime = link.minTime;
        video.rewardAmount = link.rewardAmount;
      }
    }
  }
  if (toMoney(video.rewardAmount) > 0) {
    const userId = auth.getLoginId(req);
    const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
    if (wallet) {
      const eventType = Number(activityType) === 2 ? WalletEvent.TICKET_VIDEO : WalletEvent.VIDEO;
      const record = await db.selectOne('userWalletRecord', [{ prop: 'walletId', value: wallet.id }, { prop: 'eventId', value: id }, { prop: 'eventType', value: eventType }]);
      video.getFlag = !!record;
    }
  }
  return video;
}

async function listTopicLines(req) {
  const userId = auth.getLoginId(req);
  const activity = await firstActive('topicActivity', 'topicActivity');
  if (!activity) {
    return [];
  }
  const lines = await db.selectList('topicLine', [
    { prop: 'topicActivityId', value: activity.id },
    { prop: 'status', value: true }
  ], { orderBy: 'seq ASC', limit: 7 });
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  const lightCount = wallet ? Math.floor(toMoney(wallet.amount) / 50) : 0;
  return lines.map((line) => ({ ...line, doneFlag: toNumber(line.lightSeq) <= lightCount }));
}

async function checkTopicContinue(req, id) {
  const userId = auth.getLoginId(req);
  const activity = await firstActive('topicActivity', 'topicActivity');
  if (!activity) {
    return false;
  }
  if (activity.limitNum != null) {
    const rows = await db.raw('SELECT DISTINCT topic_line_id FROM topic_record WHERE topic_activity_id = ? AND user_id = ? AND DATE(create_time) = ?', [activity.id, userId, todayString()]);
    const ids = rows.map((row) => String(row.topicLineId));
    return ids.includes(String(id)) || ids.length < toNumber(activity.limitNum);
  }
  return true;
}

async function buildTopicItemList(topic) {
  topic.topicItemList = await db.selectList('topicItem', [{ prop: 'topicId', value: topic.id }], { orderBy: 'seq ASC' });
  return topic;
}

async function listTopicsByLine(req, topicLineId) {
  const userId = auth.getLoginId(req);
  const line = await db.getById('topicLine', topicLineId);
  assertTrue(line, '地图数据不存在-请刷新重试');
  assertTrue(toNumber(line.topicNum) > 0, '当前地图活动不支持答题-请联系管理人员配置');
  const rightRows = await db.raw('SELECT topic_id FROM topic_record_topic WHERE user_id = ? AND answer_flag = true', [userId]);
  const rightIds = rightRows.map((row) => String(row.topicId));
  const params = [];
  let sql = 'SELECT * FROM topic';
  if (rightIds.length) {
    sql += ` WHERE id NOT IN (${rightIds.map(() => '?').join(',')})`;
    params.push(...rightIds);
  }
  sql += ' ORDER BY RAND() LIMIT ?';
  params.push(toNumber(line.topicNum));
  let topics = await db.raw(sql, params);
  if (rightIds.length && topics.length < toNumber(line.topicNum)) {
    const fill = await db.raw(
      `SELECT * FROM topic WHERE id IN (${rightIds.map(() => '?').join(',')}) ORDER BY RAND() LIMIT ?`,
      [...rightIds, toNumber(line.topicNum) - topics.length]
    );
    topics = topics.concat(fill);
  }
  for (const topic of topics) {
    await buildTopicItemList(topic);
  }
  const rewarded = await db.raw('SELECT id FROM topic_record WHERE user_id = ? AND topic_line_id = ? AND reward_amount > 0 LIMIT 1', [userId, line.id]);
  return { topicList: topics, firstFlag: !rewarded[0], timeFlag: true };
}

async function listRandomTopics(req) {
  const userId = auth.getLoginId(req);
  const rightRows = await db.raw('SELECT topic_id FROM topic_record_single WHERE right_flag = 1 AND user_id = ?', [userId]);
  const rightIds = rightRows.map((row) => String(row.topicId));
  const params = [];
  let sql = 'SELECT * FROM topic';
  if (rightIds.length) {
    sql += ` WHERE id NOT IN (${rightIds.map(() => '?').join(',')})`;
    params.push(...rightIds);
  }
  sql += ' ORDER BY RAND() LIMIT 10';
  const topics = await db.raw(sql, params);
  for (const topic of topics) {
    await buildTopicItemList(topic);
  }
  return topics;
}

async function addTopicRecord(req, body) {
  const userId = auth.getLoginId(req);
  const line = await db.getById('topicLine', body.topicLineId);
  assertTrue(line && Number(line.status) === 1, '当前地图活动已关闭-请刷新页面重试');
  const activity = await db.getById('topicActivity', line.topicActivityId);
  assertTrue(activity && dateValue(activity.endTime) >= new Date(), '当前活动已结束-请刷新页面重试');
  const rewarded = await db.raw('SELECT id FROM topic_record WHERE topic_line_id = ? AND user_id = ? AND reward_amount > 0 LIMIT 1', [line.id, userId]);
  const recordId = snowflakeId();
  let rewardAmount = 0;
  let rightNum = 0;
  const recordTopics = [];
  const recordItems = [];
  for (const topicVo of body.topicVoList || []) {
    const topic = await db.getById('topic', topicVo.id);
    if (!topic) {
      continue;
    }
    const recordTopicId = snowflakeId();
    let answerFlag = false;
    for (const itemVo of topicVo.recordTopicItemVoList || []) {
      const option = await db.getById('topicItem', itemVo.topicItemId);
      assertTrue(option, '未知题目选项信息-请刷新页面重新答题');
      const checked = Boolean(itemVo.checkFlag);
      if (checked && Number(option.answerFlag) === 1) {
        rightNum += 1;
        answerFlag = true;
        if (!rewarded[0]) {
          rewardAmount += toMoney(topic.rewardAmount);
        }
      }
      recordItems.push({
        topicRecordTopicId: recordTopicId,
        topicItemId: option.id,
        topicItemTitle: option.title,
        checkFlag: checked,
        answerFlag: Number(option.answerFlag) === 1
      });
    }
    recordTopics.push({
      id: recordTopicId,
      topicRecordId: recordId,
      topicId: topic.id,
      userId,
      topicTitle: topic.title,
      rewardAmount: topic.rewardAmount,
      answerFlag
    });
  }
  await db.transaction(async (conn) => {
    await db.insert('topicRecord', {
      id: recordId,
      userId,
      topicActivityId: activity.id,
      topicActivityTitle: activity.title,
      topicLineId: line.id,
      topicLineTitle: line.title,
      rewardAmount,
      usedTime: body.usedTime,
      rightNum,
      totalNum: (body.topicVoList || []).length,
      createTime: nowString()
    }, conn);
    await db.insertBatch('topicRecordTopic', recordTopics, conn);
    await db.insertBatch('topicRecordTopicItem', recordItems, conn);
    if (rewardAmount > 0) {
      checkTimeLimit();
      await rewardIntegral(userId, recordId, WalletEvent.TOPIC_REWARD, rewardAmount, conn);
    }
    await db.insert('topicRecordActivity', {
      topicActivityId: activity.id,
      userId,
      totalRewardAmount: rewardAmount
    }, conn);
  });
}

async function addSingleTopicRecord(req, body) {
  const userId = auth.getLoginId(req);
  const topic = await db.getById('topic', body.topicId);
  assertTrue(topic, '题目不存在-请刷新重试');
  const id = snowflakeId();
  let rightFlag = false;
  const items = [];
  for (const item of body.recordSingleItemList || []) {
    const option = await db.getById('topicItem', item.topicItemId);
    const answerFlag = !!(item.checkFlag && option && Number(option.answerFlag) === 1);
    if (answerFlag) {
      rightFlag = true;
    }
    items.push({
      topicRecordSingleId: id,
      topicItemId: item.topicItemId,
      topicItemTitle: option ? option.title : item.topicItemTitle,
      checkFlag: !!item.checkFlag,
      answerFlag
    });
  }
  await db.transaction(async (conn) => {
    await db.insert('topicRecordSingle', {
      id,
      topicId: topic.id,
      topicTitle: topic.title,
      userId,
      rightFlag,
      rewardAmount: topic.rewardAmount,
      createTime: nowString()
    }, conn);
    await db.insertBatch('topicRecordSingleItem', items, conn);
  });
  const result = { rewardAmount: 0 };
  if (!rightFlag || toMoney(topic.rewardAmount) < 1) {
    return result;
  }
  try {
    checkTimeLimit();
  } catch (error) {
    result.msg = error.message;
    return result;
  }
  await rewardIntegral(userId, id, WalletEvent.SINGLE_TOPIC_REWARD, topic.rewardAmount);
  result.rewardAmount = toMoney(topic.rewardAmount);
  return result;
}

async function topicRankPage(body) {
  const pageNum = toNumber(body.pageNum, 1);
  const pageSize = toNumber(body.pageSize, 15);
  return db.pageRaw(
    'SELECT uw.amount AS total_reward_amount, yu.nick_name FROM user_wallet uw LEFT JOIN yk_user yu ON uw.user_id = yu.id ORDER BY uw.amount DESC',
    'SELECT COUNT(1) AS total FROM user_wallet',
    [],
    pageNum,
    pageSize
  );
}

async function getRank(req) {
  const userId = auth.getLoginId(req);
  const wallet = await db.selectOne('userWallet', [{ prop: 'userId', value: userId }, { prop: 'type', value: INTEGRAL_TYPE }]);
  if (wallet) {
    const rows = await db.raw('SELECT COUNT(1) AS total FROM user_wallet WHERE id <> ? AND amount > ?', [wallet.id, wallet.amount]);
    return toNumber(rows[0] && rows[0].total) + 1;
  }
  return (await db.count('userWallet')) + 1;
}

async function introducePage(body) {
  const setting = cache.get('introduceSet');
  if (!setting || dateValue(setting.startTime) > new Date() || dateValue(setting.endTime) < new Date()) {
    return pageObject([], 0, body.pageNum || 1, body.pageSize || 15);
  }
  return db.page('introduce', body, {
    filters: [{ prop: 'status', value: true }],
    orderBy: 'seq ASC'
  });
}

async function createJsapiSignature(body, forceRefresh = false) {
  assertTrue(body && body.url, '签名地址不能为空');
  const ticket = await loadJsapiTicket(forceRefresh);
  const timestamp = String(Math.floor(Date.now() / 1000));
  const nonceStr = randomString(16);
  const signature = sha1(`jsapi_ticket=${ticket}&noncestr=${nonceStr}&timestamp=${timestamp}&url=${body.url}`);
  return { appId: config.wx.appId, nonceStr, timestamp, url: body.url, signature };
}

async function loadJsapiTicket(forceRefresh = false) {
  assertTrue(config.wx.appId && config.wx.secret, '微信公众号 appId 或 secret 不能为空');
  const key = `wx:mp:jsapi_ticket:${config.wx.appId}`;
  if (!forceRefresh) {
    const cached = cache.get(key);
    if (cached) {
      return cached;
    }
  }
  const accessToken = await loadAccessToken(forceRefresh);
  const res = await fetch(`https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=${encodeURIComponent(accessToken)}&type=jsapi`);
  const json = await res.json();
  if (json.errcode) {
    fail(`获取微信 jsapi_ticket 失败：${json.errmsg || json.errcode}`);
  }
  cache.set(key, json.ticket, Math.max(toNumber(json.expires_in, 7200) - 300, 60));
  return json.ticket;
}

async function loadAccessToken(forceRefresh = false) {
  const key = `wx:mp:access_token:${config.wx.appId}`;
  if (!forceRefresh) {
    const cached = cache.get(key);
    if (cached) {
      return cached;
    }
  }
  const res = await fetch(`https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${encodeURIComponent(config.wx.appId)}&secret=${encodeURIComponent(config.wx.secret)}`);
  const json = await res.json();
  if (json.errcode) {
    fail(`获取微信 access_token 失败：${json.errmsg || json.errcode}`);
  }
  cache.set(key, json.access_token, Math.max(toNumber(json.expires_in, 7200) - 300, 60));
  return json.access_token;
}

module.exports = {
  addOrModifyTimedActivity,
  feedbackPage,
  riskWarningPage,
  riskWarningRecordPage,
  topicRecordPage,
  topicRecordSinglePage,
  ticketRecordPage,
  userWalletPage,
  userWalletRecordPage,
  sysUserPage,
  sysLogin,
  addSysUser,
  modifySysUser,
  deleteSysUsers,
  setUserRole,
  deleteUserRole,
  deleteSysRoles,
  setRolePermission,
  listPermissionByRole,
  permissionTree,
  permissionTreeByPid,
  deletePermission,
  listPermissionByUser,
  areaTree,
  topicWithItems,
  saveTopic,
  deleteTopics,
  topicRecordDetail,
  singleRecordDetail,
  setTicketVideos,
  getTicketVideos,
  setVideoActivityVideos,
  getVideoActivityVideos,
  dataAnalysis,
  wxAuth,
  userInfo,
  updateUser,
  scanPay,
  checkMax,
  wxFeedbackAdd,
  wxFeedbackPage,
  wxRiskWarningPage,
  getRiskWarning,
  readRiskWarning,
  getTicketActivityOpen,
  surplusTicket,
  pageTicket,
  pageIntegral,
  doVote,
  doVideoReward,
  videoDetail,
  listTopicLines,
  checkTopicContinue,
  listTopicsByLine,
  listRandomTopics,
  addTopicRecord,
  addSingleTopicRecord,
  topicRankPage,
  getRank,
  introducePage,
  createJsapiSignature,
  loadJsapiTicket,
  firstActive,
  rewardIntegral,
  getOrCreateWallet
};
