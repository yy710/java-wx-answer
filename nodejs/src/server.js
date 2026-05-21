const config = require('./config');
const { createServer } = require('./app');

const server = createServer();

server.listen(config.port, '127.0.0.1', () => {
  console.log(`city-walk Node.js API listening on http://127.0.0.1:${config.port}`);
});
