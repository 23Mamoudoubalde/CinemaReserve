// The theater's business logic: users, orders, and the three auditoriums.
// Same rules as Main.java, without the console menus.

import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { randomBytes, scryptSync, timingSafeEqual } from 'node:crypto';
import { Hashmap } from './Hashmap.js';
import { Auditorium } from './Auditorium.js';

export const PRICES = { A: 10.0, C: 5.0, S: 7.5 };
const AUDITORIUM_COUNT = 3;

export class HttpError extends Error {
  constructor(status, message, details = {}) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

const seatLabel = (row, seat) => `${row}${String.fromCharCode(65 + seat)}`;

function hashPassword(password, salt = randomBytes(16)) {
  return { salt, hash: scryptSync(password, salt, 32) };
}

// Turns { adult, child, senior } into a list like ['A','A','C'], in the
// same order the Java version reserved them (adults, then children, then seniors).
function ticketList({ adult = 0, child = 0, senior = 0 }) {
  const counts = { A: adult, C: child, S: senior };
  for (const n of Object.values(counts)) {
    if (!Number.isInteger(n) || n < 0) throw new HttpError(400, 'Ticket counts must be whole numbers 0 or more');
  }
  const list = Object.entries(counts).flatMap(([type, n]) => Array(n).fill(type));
  if (list.length === 0) throw new HttpError(400, 'Select at least one ticket');
  return list;
}

export class Theater {
  constructor(dataDir) {
    this.users = new Hashmap();
    this.nextOrderId = 1;

    for (const line of readFileSync(join(dataDir, 'userdb.dat'), 'utf8').split(/\r?\n/)) {
      const [username, password] = line.trim().split(/\s+/);
      if (!username || !password) continue;
      this.users.put(username, { username, ...hashPassword(password), orders: [] });
    }

    this.auditoriums = [];
    for (let i = 1; i <= AUDITORIUM_COUNT; i++) {
      this.auditoriums.push(Auditorium.fromFile(i, join(dataDir, `A${i}.txt`)));
    }
  }

  authenticate(username, password) {
    const user = typeof username === 'string' ? this.users.get(username) : null;
    if (!user || typeof password !== 'string') return null;
    const { hash } = hashPassword(password, user.salt);
    return timingSafeEqual(hash, user.hash) ? user : null;
  }

  auditorium(id) {
    const a = this.auditoriums[Number(id) - 1];
    if (!a) throw new HttpError(404, 'Auditorium not found');
    return a;
  }

  #user(username) {
    const user = this.users.get(username);
    if (!user) throw new HttpError(404, 'User not found');
    return user;
  }

  #order(username, orderId) {
    const order = this.#user(username).orders.find((o) => o.id === Number(orderId));
    if (!order) throw new HttpError(404, 'Order not found');
    return order;
  }

  // Reserves the block of seats, or throws 409 with the best available block.
  #reserveBlock(auditorium, row, seat, tickets) {
    if (!auditorium.isAvailable(row, seat, tickets.length)) {
      throw new HttpError(409, 'Those seats are not available', {
        bestAvailable: auditorium.findBestAvailable(tickets.length),
      });
    }
    return tickets.map((type, i) => {
      auditorium.reserve(row, seat + i, type);
      return { row, seat: seat + i, type };
    });
  }

  createOrder(username, { auditorium: id, row, seat, ...counts }) {
    const user = this.#user(username);
    const auditorium = this.auditorium(id);
    const seats = this.#reserveBlock(auditorium, row, seat, ticketList(counts));
    const order = { id: this.nextOrderId++, auditorium: auditorium.id, seats };
    user.orders.push(order);
    return this.#describe(order);
  }

  addTickets(username, orderId, { row, seat, ...counts }) {
    const order = this.#order(username, orderId);
    const auditorium = this.auditorium(order.auditorium);
    order.seats.push(...this.#reserveBlock(auditorium, row, seat, ticketList(counts)));
    return this.#describe(order);
  }

  removeSeat(username, orderId, row, seat) {
    const user = this.#user(username);
    const order = this.#order(username, orderId);
    const i = order.seats.findIndex((s) => s.row === row && s.seat === seat);
    if (i === -1) throw new HttpError(404, 'That seat is not in this order');
    this.auditorium(order.auditorium).unreserve(row, seat);
    order.seats.splice(i, 1);
    if (order.seats.length === 0) user.orders = user.orders.filter((o) => o !== order);
    return order.seats.length ? this.#describe(order) : null;
  }

  cancelOrder(username, orderId) {
    const user = this.#user(username);
    const order = this.#order(username, orderId);
    const auditorium = this.auditorium(order.auditorium);
    for (const s of order.seats) auditorium.unreserve(s.row, s.seat);
    user.orders = user.orders.filter((o) => o !== order);
  }

  #describe(order) {
    const seats = [...order.seats].sort((a, b) => a.row - b.row || a.seat - b.seat);
    const count = (t) => seats.filter((s) => s.type === t).length;
    return {
      id: order.id,
      auditorium: order.auditorium,
      seats: seats.map((s) => ({ ...s, label: seatLabel(s.row, s.seat) })),
      adult: count('A'),
      child: count('C'),
      senior: count('S'),
      total: seats.reduce((sum, s) => sum + PRICES[s.type], 0),
    };
  }

  orders(username) {
    const orders = this.#user(username).orders.map((o) => this.#describe(o));
    return { orders, customerTotal: orders.reduce((sum, o) => sum + o.total, 0) };
  }

  report() {
    const rows = this.auditoriums.map((a) => {
      const s = a.stats();
      return { auditorium: a.id, ...s, sales: s.adult * PRICES.A + s.child * PRICES.C + s.senior * PRICES.S };
    });
    const total = rows.reduce((t, r) => {
      for (const k of ['open', 'reserved', 'adult', 'child', 'senior', 'sales']) t[k] += r[k];
      return t;
    }, { open: 0, reserved: 0, adult: 0, child: 0, senior: 0, sales: 0 });
    return { auditoriums: rows, total };
  }

  save(outDir) {
    return this.auditoriums.map((a) => {
      const file = join(outDir, `A${a.id}Final.txt`);
      a.saveToFile(file);
      return file;
    });
  }
}
