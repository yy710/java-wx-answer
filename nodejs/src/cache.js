const store = new Map();

function get(key) {
  const item = store.get(key);
  if (!item) {
    return null;
  }
  if (item.expiresAt && Date.now() > item.expiresAt) {
    store.delete(key);
    return null;
  }
  return item.value;
}

function set(key, value, ttlSeconds) {
  const expiresAt = ttlSeconds ? Date.now() + ttlSeconds * 1000 : null;
  store.set(key, { value, expiresAt });
  return value;
}

function del(key) {
  store.delete(key);
}

function increment(key, by = 1, defaultValue = 0) {
  const next = Number(get(key) == null ? defaultValue : get(key)) + by;
  set(key, next);
  return next;
}

module.exports = {
  get,
  set,
  del,
  increment
};
