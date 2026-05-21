const crypto = require('crypto');
const path = require('path');

let sequence = 0n;

function snowflakeId() {
  sequence = (sequence + 1n) % 10000n;
  return (BigInt(Date.now()) * 10000n + sequence).toString();
}

function md5(value) {
  return crypto.createHash('md5').update(String(value || '')).digest('hex');
}

function sha1(value) {
  return crypto.createHash('sha1').update(String(value || '')).digest('hex');
}

function randomString(length = 16) {
  const alphabet = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
  let out = '';
  const bytes = crypto.randomBytes(length);
  for (let i = 0; i < length; i += 1) {
    out += alphabet[bytes[i] % alphabet.length];
  }
  return out;
}

function camelToSnake(value) {
  return String(value).replace(/[A-Z]/g, (char) => `_${char.toLowerCase()}`).replace(/^_/, '');
}

function snakeToCamel(value) {
  return String(value).replace(/_+([a-zA-Z0-9])/g, (_, char) => char.toUpperCase());
}

function normalizeRow(row) {
  if (!row || typeof row !== 'object') {
    return row;
  }
  const result = {};
  for (const [key, value] of Object.entries(row)) {
    result[snakeToCamel(key)] = value;
  }
  return result;
}

function normalizeRows(rows) {
  return Array.isArray(rows) ? rows.map(normalizeRow) : rows;
}

function nowString(date = new Date()) {
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function todayString(date = new Date()) {
  return nowString(date).slice(0, 10);
}

function toNumber(value, fallback = 0) {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

function toMoney(value) {
  const n = Number(value || 0);
  return Number.isFinite(n) ? n : 0;
}

function extFromFilename(filename) {
  const ext = path.extname(filename || '');
  return ext || '.bin';
}

function pageObject(records, total, pageNum, pageSize) {
  const size = toNumber(pageSize, 15);
  const current = toNumber(pageNum, 1);
  return {
    records,
    total,
    size,
    current,
    pages: size > 0 ? Math.ceil(total / size) : 0
  };
}

function buildTree(list, parentId = '0', parentKey = 'pid', idKey = 'id') {
  return list
    .filter((item) => String(item[parentKey] == null ? '' : item[parentKey]) === String(parentId))
    .sort((a, b) => toNumber(a.sort, 0) - toNumber(b.sort, 0))
    .map((item) => {
      const children = buildTree(list, item[idKey], parentKey, idKey);
      return children.length > 0 ? { ...item, children } : { ...item };
    });
}

function normalizeAvatarUrl(url) {
  if (!url || typeof url !== 'string') {
    return url;
  }
  return url.startsWith('http://') ? `https://${url.slice('http://'.length)}` : url;
}

module.exports = {
  snowflakeId,
  md5,
  sha1,
  randomString,
  camelToSnake,
  snakeToCamel,
  normalizeRow,
  normalizeRows,
  nowString,
  todayString,
  toNumber,
  toMoney,
  extFromFilename,
  pageObject,
  buildTree,
  normalizeAvatarUrl
};
