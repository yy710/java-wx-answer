class YunKeError extends Error {
  constructor(message = '失败', code = 500) {
    super(message);
    this.name = 'YunKeError';
    this.code = code;
  }
}

class UnauthorizedError extends Error {
  constructor(message = '当前会话未登录') {
    super(message);
    this.name = 'UnauthorizedError';
    this.code = 401;
  }
}

module.exports = {
  YunKeError,
  UnauthorizedError
};
