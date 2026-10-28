// Express REST API for CinemaReserve.

import express from 'express';
import { existsSync } from 'node:fs';
import { randomBytes } from 'node:crypto';
import { HttpError } from './Theater.js';

const MAX_ATTEMPTS = 3; // same limit as the Java login prompt
const LOCKOUT_MS = 60_000;

export function createApp(theater, { dataDir, clientDir } = {}) {
  const app = express();
  const sessions = new Map(); // token -> username
  const failedLogins = new Map(); // username -> { count, lockedUntil }

  app.use(express.json());

  const toInt = (v) => (typeof v === 'string' && /^\d+$/.test(v) ? Number(v) : v);

  function requireUser(req, res, next) {
    const token = req.get('authorization')?.replace(/^Bearer /, '');
    const username = token && sessions.get(token);
    if (!username) return next(new HttpError(401, 'Please log in'));
    req.username = username;
    next();
  }

  function requireAdmin(req, res, next) {
    if (req.username !== 'admin') return next(new HttpError(403, 'Admins only'));
    next();
  }

  // ---- Auth ----
  app.post('/api/login', (req, res) => {
    const { username, password } = req.body ?? {};
    const record = failedLogins.get(username);
    if (record?.lockedUntil > Date.now()) {
      throw new HttpError(429, 'Too many failed attempts. Try again in a minute.');
    }
    const user = theater.authenticate(username, password);
    if (!user) {
      const count = (record?.count ?? 0) + 1;
      failedLogins.set(username, {
        count: count >= MAX_ATTEMPTS ? 0 : count,
        lockedUntil: count >= MAX_ATTEMPTS ? Date.now() + LOCKOUT_MS : 0,
      });
      throw new HttpError(401, 'Invalid username or password');
    }
    failedLogins.delete(username);
    const token = randomBytes(24).toString('hex');
    sessions.set(token, user.username);
    res.json({ token, username: user.username, role: user.username === 'admin' ? 'admin' : 'customer' });
  });

  app.post('/api/logout', requireUser, (req, res) => {
    sessions.delete(req.get('authorization').replace(/^Bearer /, ''));
    res.status(204).end();
  });

  // ---- Seats ----
  app.get('/api/auditoriums', requireUser, (req, res) => {
    res.json(theater.auditoriums.map((a) => ({
      id: a.id, rows: a.numRows, seatsPerRow: a.seatsPerRow, open: a.stats().open,
    })));
  });

  app.get('/api/auditoriums/:id/seats', requireUser, (req, res) => {
    const a = theater.auditorium(req.params.id);
    res.json({ id: a.id, rows: a.numRows, seatsPerRow: a.seatsPerRow, taken: a.seatMap() });
  });

  app.get('/api/auditoriums/:id/best', requireUser, (req, res) => {
    const best = theater.auditorium(req.params.id).findBestAvailable(toInt(req.query.count));
    if (!best) throw new HttpError(404, 'No block of seats that size is available');
    res.json(best);
  });

  // ---- Orders (customers) ----
  app.get('/api/orders', requireUser, (req, res) => {
    res.json(theater.orders(req.username));
  });

  app.post('/api/orders', requireUser, (req, res) => {
    res.status(201).json(theater.createOrder(req.username, req.body ?? {}));
  });

  app.post('/api/orders/:id/tickets', requireUser, (req, res) => {
    res.json(theater.addTickets(req.username, req.params.id, req.body ?? {}));
  });

  app.delete('/api/orders/:id/seats/:row/:seat', requireUser, (req, res) => {
    const order = theater.removeSeat(req.username, req.params.id, toInt(req.params.row), toInt(req.params.seat));
    order ? res.json(order) : res.status(204).end();
  });

  app.delete('/api/orders/:id', requireUser, (req, res) => {
    theater.cancelOrder(req.username, req.params.id);
    res.status(204).end();
  });

  // ---- Admin ----
  app.get('/api/admin/report', requireUser, requireAdmin, (req, res) => {
    res.json(theater.report());
  });

  app.post('/api/admin/save', requireUser, requireAdmin, (req, res) => {
    res.json({ saved: theater.save(dataDir).map((f) => f.split(/[\\/]/).pop()) });
  });

  app.use('/api', (req, res, next) => next(new HttpError(404, 'Not found')));

  // Serve the built React app, if it exists
  if (clientDir && existsSync(clientDir)) {
    app.use(express.static(clientDir));
    app.get(/^(?!\/api).*/, (req, res) => res.sendFile('index.html', { root: clientDir }));
  }

  // eslint-disable-next-line no-unused-vars
  app.use((err, req, res, next) => {
    const status = err.status ?? 500;
    if (status === 500) console.error(err);
    res.status(status).json({ error: status === 500 ? 'Something went wrong' : err.message, ...err.details });
  });

  return app;
}
