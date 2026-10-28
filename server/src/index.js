import { fileURLToPath } from 'node:url';
import { Theater } from './Theater.js';
import { createApp } from './app.js';

const dataDir = process.env.DATA_DIR ?? fileURLToPath(new URL('../data', import.meta.url));
const clientDir = fileURLToPath(new URL('../../client/dist', import.meta.url));
const port = Number(process.env.PORT ?? 3001);

const theater = new Theater(dataDir);
createApp(theater, { dataDir, clientDir }).listen(port, () => {
  console.log(`CinemaReserve API running on http://localhost:${port}`);
});
