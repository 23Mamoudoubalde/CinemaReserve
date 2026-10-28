import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { cpSync, mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { Theater } from '../src/Theater.js';
import { createApp } from '../src/app.js';

let server, base, dataDir;

before(async () => {
  // Work on a copy of the sample data so tests never change the real files
  dataDir = mkdtempSync(join(tmpdir(), 'cinema-'));
  cpSync(fileURLToPath(new URL('../data', import.meta.url)), dataDir, { recursive: true });
  const app = createApp(new Theater(dataDir), { dataDir });
  server = app.listen(0);
  await new Promise((r) => server.once('listening', r));
  base = `http://localhost:${server.address().port}/api`;
});

after(() => {
  server.close();
  rmSync(dataDir, { recursive: true, force: true });
});

async function call(method, path, { token, body } = {}) {
  const res = await fetch(base + path, {
    method,
    headers: {
      'content-type': 'application/json',
      ...(token && { authorization: `Bearer ${token}` }),
    },
    body: body && JSON.stringify(body),
  });
  const text = await res.text();
  return { status: res.status, body: text ? JSON.parse(text) : null };
}

const login = async (username, password) =>
  (await call('POST', '/login', { body: { username, password } })).body.token;

test('rejects bad credentials and requests without a token', async () => {
  const bad = await call('POST', '/login', { body: { username: 'mamoudou', password: 'nope' } });
  assert.equal(bad.status, 401);
  assert.equal(bad.body.error, 'Invalid username or password');
  assert.equal((await call('GET', '/orders')).status, 401);
});

test('locks a username after 3 failed logins', async () => {
  for (let i = 0; i < 3; i++) await call('POST', '/login', { body: { username: 'alex', password: 'x' } });
  const locked = await call('POST', '/login', { body: { username: 'alex', password: 'test456' } });
  assert.equal(locked.status, 429);
});

test('customer can book, add to, trim and cancel an order', async () => {
  const token = await login('mamoudou', 'demo123');

  const seats = await call('GET', '/auditoriums/2/seats', { token });
  assert.equal(seats.status, 200);
  assert.equal(seats.body.taken[1][3], true); // row 2, seat D is taken in A2.txt

  // Book row 1, seats A-C: 2 adults + 1 child = $25
  const created = await call('POST', '/orders', {
    token, body: { auditorium: 2, row: 1, seat: 0, adult: 2, child: 1, senior: 0 },
  });
  assert.equal(created.status, 201);
  assert.deepEqual(created.body.seats.map((s) => s.label), ['1A', '1B', '1C']);
  assert.equal(created.body.total, 25);
  const id = created.body.id;

  // Same seats again -> 409 with a suggestion
  const clash = await call('POST', '/orders', {
    token, body: { auditorium: 2, row: 1, seat: 1, adult: 2 },
  });
  assert.equal(clash.status, 409);
  assert.ok(clash.body.bestAvailable);

  // Add a senior in row 3
  const added = await call('POST', `/orders/${id}/tickets`, {
    token, body: { row: 3, seat: 5, senior: 1 },
  });
  assert.equal(added.body.senior, 1);
  assert.equal(added.body.total, 32.5);

  // Remove one seat
  const trimmed = await call('DELETE', `/orders/${id}/seats/1/0`, { token });
  assert.deepEqual(trimmed.body.seats.map((s) => s.label), ['1B', '1C', '3F']);

  const list = await call('GET', '/orders', { token });
  assert.equal(list.body.customerTotal, 22.5);

  // Cancel frees every seat
  assert.equal((await call('DELETE', `/orders/${id}`, { token })).status, 204);
  const after = await call('GET', '/auditoriums/2/seats', { token });
  assert.equal(after.body.taken[0][1], false);
  assert.equal(after.body.taken[2][5], false);
});

test('validates input', async () => {
  const token = await login('mamoudou', 'demo123');
  const none = await call('POST', '/orders', { token, body: { auditorium: 1, row: 1, seat: 0 } });
  assert.equal(none.status, 400);
  const negative = await call('POST', '/orders', { token, body: { auditorium: 1, row: 1, seat: 0, adult: -1 } });
  assert.equal(negative.status, 400);
  const offMap = await call('POST', '/orders', { token, body: { auditorium: 1, row: 99, seat: 0, adult: 1 } });
  assert.equal(offMap.status, 409);
  const noRoom = await call('POST', '/orders', { token, body: { auditorium: 9, row: 1, seat: 0, adult: 1 } });
  assert.equal(noRoom.status, 404);
});

test('customers cannot touch other customers orders', async () => {
  const mine = await login('mamoudou', 'demo123');
  const order = await call('POST', '/orders', { token: mine, body: { auditorium: 1, row: 5, seat: 0, adult: 1 } });
  const other = await login('admin', 'admin123');
  assert.equal((await call('DELETE', `/orders/${order.body.id}`, { token: other })).status, 404);
});

test('admin report and save; customers are blocked', async () => {
  const customer = await login('mamoudou', 'demo123');
  assert.equal((await call('GET', '/admin/report', { token: customer })).status, 403);

  const admin = await login('admin', 'admin123');
  const report = await call('GET', '/admin/report', { token: admin });
  assert.equal(report.status, 200);
  assert.equal(report.body.auditoriums.length, 3);
  const t = report.body.total;
  assert.equal(t.open + t.reserved, 8 * 14 + 6 * 10 + 5 * 14);

  const saved = await call('POST', '/admin/save', { token: admin });
  assert.deepEqual(saved.body.saved, ['A1Final.txt', 'A2Final.txt', 'A3Final.txt']);
  assert.equal(readFileSync(join(dataDir, 'A2Final.txt'), 'utf8'), readFileSync(join(dataDir, 'A2.txt'), 'utf8'));
});
