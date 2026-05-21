const ResultCode = {
  SUCCESS: { code: 200, message: '操作成功' },
  FAILED: { code: 500, message: '操作失败' },
  VALIDATE_FAILED: { code: 501, message: '参数检验失败' },
  UNAUTHORIZED: { code: 401, message: '暂未登录或token已经过期' },
  FORBIDDEN: { code: 403, message: '没有相关权限' },
  UNREGISTERED: { code: 5001, message: '账号未注册' }
};

function ok(data = null, message = ResultCode.SUCCESS.message) {
  return { code: ResultCode.SUCCESS.code, message, data };
}

function status(flag) {
  return ok(flag ? '操作成功' : '操作失败', flag ? ResultCode.SUCCESS.message : ResultCode.FAILED.message);
}

function failed(message = ResultCode.FAILED.message, code = ResultCode.FAILED.code) {
  return { code, message, data: null };
}

function validateFailed(message = ResultCode.VALIDATE_FAILED.message) {
  return failed(message, ResultCode.VALIDATE_FAILED.code);
}

function unauthorized(data = '当前会话未登录') {
  return { code: ResultCode.UNAUTHORIZED.code, message: ResultCode.UNAUTHORIZED.message, data };
}

module.exports = {
  ResultCode,
  ok,
  status,
  failed,
  validateFailed,
  unauthorized
};
