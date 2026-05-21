const config = require('./config');
const { UnauthorizedError } = require('./errors');
const { randomString } = require('./utils');

const sessions = new Map();

function createToken(loginId, type = 'user') {
  const tokenValue = `tik-${randomString(32)}`;
  const token = {
    tokenName: config.tokenName,
    tokenValue,
    loginId: String(loginId),
    type,
    loginType: type,
    tokenTimeout: 2592000,
    sessionTimeout: 2592000,
    tokenSessionTimeout: 2592000,
    tokenActivityTimeout: -1,
    loginDevice: 'default',
    tag: null
  };
  sessions.set(tokenValue, token);
  return token;
}

function readToken(req) {
  const headers = req.headers || {};
  const bearer = headers.authorization || headers.Authorization;
  if (bearer && /^Bearer\s+/i.test(bearer)) {
    return bearer.replace(/^Bearer\s+/i, '').trim();
  }
  return headers[config.tokenName.toLowerCase()] || headers[config.tokenName] || headers.token || null;
}

function getSession(req) {
  const tokenValue = readToken(req);
  if (!tokenValue) {
    return null;
  }
  return sessions.get(String(tokenValue)) || null;
}

function getLoginId(req) {
  const session = getSession(req);
  if (!session) {
    throw new UnauthorizedError('未提供token');
  }
  return session.loginId;
}

function maybeLoginId(req) {
  const session = getSession(req);
  return session ? session.loginId : null;
}

function logout(req) {
  const tokenValue = readToken(req);
  if (tokenValue) {
    sessions.delete(String(tokenValue));
  }
}

module.exports = {
  createToken,
  readToken,
  getSession,
  getLoginId,
  maybeLoginId,
  logout
};
