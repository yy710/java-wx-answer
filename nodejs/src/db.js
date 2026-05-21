const config = require('./config');
const { models } = require('./models');
const { camelToSnake, normalizeRow, normalizeRows, nowString, pageObject, snowflakeId, toNumber } = require('./utils');

let pool;

function getMysql() {
  try {
    return require('mysql2/promise');
  } catch (error) {
    const message = '缺少 mysql2 依赖，请在 nodejs 目录执行 npm install 后再连接数据库';
    const wrapped = new Error(message);
    wrapped.cause = error;
    throw wrapped;
  }
}

function getPool() {
  if (!pool) {
    const mysql = getMysql();
    pool = mysql.createPool({
      ...config.db,
      waitForConnections: true,
      connectionLimit: Number(process.env.DB_POOL_SIZE || 10),
      supportBigNumbers: true,
      bigNumberStrings: true,
      dateStrings: true,
      namedPlaceholders: false
    });
  }
  return pool;
}

function quote(identifier) {
  return `\`${String(identifier).replace(/`/g, '``')}\``;
}

function getModel(modelKey) {
  const m = models[modelKey];
  if (!m) {
    throw new Error(`未知模型: ${modelKey}`);
  }
  return m;
}

function propToColumn(m, prop) {
  if (m.columns[prop]) {
    return m.columns[prop];
  }
  const snake = camelToSnake(prop);
  if (m.byColumn[snake]) {
    return snake;
  }
  if (m.byColumn[prop]) {
    return prop;
  }
  return null;
}

function rowToDb(m, data, options = {}) {
  const out = {};
  for (const [prop, value] of Object.entries(data || {})) {
    if (value === undefined || (options.skipNull && value === null)) {
      continue;
    }
    if (!options.includeId && prop === m.idProp) {
      continue;
    }
    const column = propToColumn(m, prop);
    if (column) {
      out[column] = value;
    }
  }
  return out;
}

async function query(sql, params = [], conn) {
  const executor = conn || getPool();
  const [rows] = await executor.execute(sql, params);
  return rows;
}

async function raw(sql, params = [], conn) {
  const rows = await query(sql, params, conn);
  return normalizeRows(rows);
}

async function transaction(fn) {
  const conn = await getPool().getConnection();
  try {
    await conn.beginTransaction();
    const result = await fn(conn);
    await conn.commit();
    return result;
  } catch (error) {
    await conn.rollback();
    throw error;
  } finally {
    conn.release();
  }
}

function buildWhere(m, filters = []) {
  const clauses = [];
  const params = [];

  for (const filter of filters) {
    if (!filter) {
      continue;
    }
    if (filter.raw) {
      clauses.push(filter.raw);
      if (Array.isArray(filter.params)) {
        params.push(...filter.params);
      }
      continue;
    }
    const column = filter.column || propToColumn(m, filter.prop);
    if (!column) {
      continue;
    }
    const value = filter.value;
    if (value === undefined || value === '') {
      continue;
    }
    const q = filter.qualified ? filter.qualified : quote(column);
    switch (filter.op || 'eq') {
      case 'like':
        clauses.push(`${q} LIKE ?`);
        params.push(`%${value}%`);
        break;
      case 'gt':
        clauses.push(`${q} > ?`);
        params.push(value);
        break;
      case 'gte':
        clauses.push(`${q} >= ?`);
        params.push(value);
        break;
      case 'lt':
        clauses.push(`${q} < ?`);
        params.push(value);
        break;
      case 'lte':
        clauses.push(`${q} <= ?`);
        params.push(value);
        break;
      case 'ne':
        clauses.push(`${q} <> ?`);
        params.push(value);
        break;
      case 'in':
        if (Array.isArray(value) && value.length > 0) {
          clauses.push(`${q} IN (${value.map(() => '?').join(',')})`);
          params.push(...value);
        }
        break;
      case 'dateEq':
        clauses.push(`DATE(${q}) = ?`);
        params.push(value);
        break;
      default:
        if (value === null) {
          clauses.push(`${q} IS NULL`);
        } else {
          clauses.push(`${q} = ?`);
          params.push(value);
        }
    }
  }

  return {
    sql: clauses.length ? ` WHERE ${clauses.join(' AND ')}` : '',
    params
  };
}

function filtersFromData(modelKey, data = {}, options = {}) {
  const m = getModel(modelKey);
  const filters = [];
  const likeFields = options.likeFields || m.likeFields || [];
  for (const [prop, value] of Object.entries(data || {})) {
    if (['pageNum', 'pageSize', 'searchMap', 'startSortId', 'endSortId', 'sortFlag'].includes(prop)) {
      continue;
    }
    if (value === undefined || value === null || value === '') {
      continue;
    }
    const column = propToColumn(m, prop);
    if (!column) {
      continue;
    }
    filters.push({ prop, value, op: likeFields.includes(prop) ? 'like' : 'eq' });
  }
  return filters;
}

async function selectList(modelKey, filters = [], options = {}, conn) {
  const m = getModel(modelKey);
  const where = buildWhere(m, filters);
  const order = options.orderBy || m.defaultOrder;
  const limit = options.limit ? ` LIMIT ${Number(options.limit)}` : '';
  const orderSql = order ? ` ORDER BY ${order}` : '';
  const sql = `SELECT * FROM ${quote(m.table)}${where.sql}${orderSql}${limit}`;
  const rows = await query(sql, where.params, conn);
  return normalizeRows(rows);
}

async function selectOne(modelKey, filters = [], options = {}, conn) {
  const rows = await selectList(modelKey, filters, { ...options, limit: 1 }, conn);
  return rows[0] || null;
}

async function getById(modelKey, id, conn) {
  const m = getModel(modelKey);
  return selectOne(modelKey, [{ column: m.idColumn, value: id }], {}, conn);
}

async function count(modelKey, filters = [], conn) {
  const m = getModel(modelKey);
  const where = buildWhere(m, filters);
  const rows = await query(`SELECT COUNT(1) AS total FROM ${quote(m.table)}${where.sql}`, where.params, conn);
  return toNumber(rows[0] && rows[0].total, 0);
}

async function sum(modelKey, column, filters = [], conn) {
  const m = getModel(modelKey);
  const dbColumn = propToColumn(m, column) || column;
  const where = buildWhere(m, filters);
  const rows = await query(`SELECT SUM(${quote(dbColumn)}) AS total FROM ${quote(m.table)}${where.sql}`, where.params, conn);
  return Number((rows[0] && rows[0].total) || 0);
}

async function page(modelKey, data = {}, options = {}, conn) {
  const m = getModel(modelKey);
  const pageNum = toNumber(data.pageNum || options.pageNum, 1);
  const pageSize = toNumber(data.pageSize || options.pageSize, 15);
  const filters = options.filters || filtersFromData(modelKey, data, options);
  const where = buildWhere(m, filters);
  const totalRows = await query(`SELECT COUNT(1) AS total FROM ${quote(m.table)}${where.sql}`, where.params, conn);
  const total = toNumber(totalRows[0] && totalRows[0].total, 0);
  const offset = (pageNum - 1) * pageSize;
  const order = options.orderBy || m.defaultOrder || (m.idColumn ? `${quote(m.idColumn)} DESC` : '');
  const orderSql = order ? ` ORDER BY ${order}` : '';
  const rows = await query(
    `SELECT * FROM ${quote(m.table)}${where.sql}${orderSql} LIMIT ? OFFSET ?`,
    [...where.params, pageSize, offset],
    conn
  );
  return pageObject(normalizeRows(rows), total, pageNum, pageSize);
}

async function pageRaw(sql, countSql, params = [], pageNum = 1, pageSize = 15, conn) {
  const totalRows = await query(countSql, params, conn);
  const total = toNumber(totalRows[0] && totalRows[0].total, 0);
  const rows = await query(`${sql} LIMIT ? OFFSET ?`, [...params, toNumber(pageSize, 15), (toNumber(pageNum, 1) - 1) * toNumber(pageSize, 15)], conn);
  return pageObject(normalizeRows(rows), total, pageNum, pageSize);
}

async function insert(modelKey, data, conn) {
  const m = getModel(modelKey);
  const body = { ...data };
  if (m.idColumn && !m.keys && !body[m.idProp]) {
    body[m.idProp] = snowflakeId();
  }
  if (m.columns.createTime && body.createTime === undefined) {
    body.createTime = nowString();
  }
  const dbData = rowToDb(m, body, { includeId: true });
  const columns = Object.keys(dbData);
  const values = Object.values(dbData);
  if (!columns.length) {
    throw new Error(`插入 ${modelKey} 时没有可写字段`);
  }
  await query(
    `INSERT INTO ${quote(m.table)} (${columns.map(quote).join(',')}) VALUES (${columns.map(() => '?').join(',')})`,
    values,
    conn
  );
  return { ...body };
}

async function insertBatch(modelKey, rows, conn) {
  if (!rows || rows.length === 0) {
    return true;
  }
  for (const row of rows) {
    await insert(modelKey, row, conn);
  }
  return true;
}

async function updateById(modelKey, id, data, conn) {
  const m = getModel(modelKey);
  const body = { ...data };
  if (m.columns.updateTime && body.updateTime === undefined) {
    body.updateTime = nowString();
  }
  const dbData = rowToDb(m, body, { includeId: false, skipNull: true });
  const columns = Object.keys(dbData);
  if (!columns.length) {
    return true;
  }
  await query(
    `UPDATE ${quote(m.table)} SET ${columns.map((column) => `${quote(column)} = ?`).join(',')} WHERE ${quote(m.idColumn)} = ?`,
    [...Object.values(dbData), id],
    conn
  );
  return true;
}

async function saveOrUpdate(modelKey, data, conn) {
  const m = getModel(modelKey);
  if (data && data[m.idProp]) {
    await updateById(modelKey, data[m.idProp], data, conn);
    return { ...data };
  }
  return insert(modelKey, data, conn);
}

async function deleteByIds(modelKey, ids, conn) {
  const m = getModel(modelKey);
  const values = Array.isArray(ids) ? ids : [ids];
  if (!values.length) {
    return true;
  }
  await query(
    `DELETE FROM ${quote(m.table)} WHERE ${quote(m.idColumn)} IN (${values.map(() => '?').join(',')})`,
    values,
    conn
  );
  return true;
}

async function deleteWhere(modelKey, filters = [], conn) {
  const m = getModel(modelKey);
  const where = buildWhere(m, filters);
  if (!where.sql) {
    throw new Error('拒绝无条件删除');
  }
  await query(`DELETE FROM ${quote(m.table)}${where.sql}`, where.params, conn);
  return true;
}

async function updateWhere(modelKey, filters = [], data = {}, conn) {
  const m = getModel(modelKey);
  const dbData = rowToDb(m, data, { includeId: false, skipNull: true });
  const columns = Object.keys(dbData);
  if (!columns.length) {
    return true;
  }
  const where = buildWhere(m, filters);
  if (!where.sql) {
    throw new Error('拒绝无条件更新');
  }
  await query(
    `UPDATE ${quote(m.table)} SET ${columns.map((column) => `${quote(column)} = ?`).join(',')}${where.sql}`,
    [...Object.values(dbData), ...where.params],
    conn
  );
  return true;
}

module.exports = {
  getPool,
  query,
  raw,
  transaction,
  getModel,
  propToColumn,
  filtersFromData,
  buildWhere,
  selectList,
  selectOne,
  getById,
  count,
  sum,
  page,
  pageRaw,
  insert,
  insertBatch,
  updateById,
  saveOrUpdate,
  deleteByIds,
  deleteWhere,
  updateWhere,
  quote
};
