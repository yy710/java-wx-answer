const fs = require('fs');
const path = require('path');

function loadEnvFile() {
  const envPath = path.join(__dirname, '..', '.env');
  if (!fs.existsSync(envPath)) {
    return;
  }
  const lines = fs.readFileSync(envPath, 'utf8').split(/\r?\n/);
  for (const line of lines) {
    if (!line || line.trim().startsWith('#')) {
      continue;
    }
    const index = line.indexOf('=');
    if (index < 0) {
      continue;
    }
    const key = line.slice(0, index).trim();
    const value = line.slice(index + 1).trim();
    if (!Object.prototype.hasOwnProperty.call(process.env, key)) {
      process.env[key] = value;
    }
  }
}

loadEnvFile();

module.exports = {
  port: Number(process.env.PORT || 6003),
  tokenName: process.env.TOKEN_NAME || 'hy-token',
  publicFileBaseUrl: process.env.PUBLIC_FILE_BASE_URL || `http://127.0.0.1:${process.env.PORT || 6003}`,
  db: {
    host: process.env.DB_HOST || '127.0.0.1',
    port: Number(process.env.DB_PORT || 3306),
    database: process.env.DB_NAME || 'city-walk',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '123456'
  },
  wx: {
    appId: process.env.WX_MP_APP_ID || '',
    secret: process.env.WX_MP_SECRET || ''
  },
  cos: {
    bucket: process.env.COS_BUCKET || '',
    region: process.env.COS_REGION || '',
    accessKey: process.env.COS_ACCESS_KEY || '',
    secretKey: process.env.COS_SECRET_KEY || ''
  }
};
