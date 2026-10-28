import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Hashmap } from '../src/Hashmap.js';
import { Auditorium } from '../src/Auditorium.js';

test('Hashmap stores, updates and removes keys', () => {
  const map = new Hashmap();
  map.put('alice', 1);
  map.put('alice', 2);
  assert.equal(map.get('alice'), 2);
  assert.equal(map.size(), 1);
  map.remove('alice');
  assert.equal(map.containsKey('alice'), false);
  assert.equal(map.get('missing'), null);
});

test('Hashmap rehashes past the load factor and keeps every entry', () => {
  const map = new Hashmap();
  for (let i = 0; i < 100; i++) map.put(`user${i}`, i);
  assert.ok(map.bucketCount() > 5, 'table should have grown');
  for (let i = 0; i < 100; i++) assert.equal(map.get(`user${i}`), i);
  assert.equal(map.size(), 100);
});

test('Auditorium loads layout into a 2D linked list', () => {
  const a = new Auditorium(1, 'A..\n.CS\n');
  assert.equal(a.numRows, 2);
  assert.equal(a.seatsPerRow, 3);
  assert.equal(a.ticketTypeAt(1, 0), 'A');
  assert.equal(a.ticketTypeAt(2, 2), 'S');
  assert.deepEqual(a.seatMap(), [[true, false, false], [false, true, true]]);
  assert.equal(a.toText(), 'A..\n.CS\n');
});

test('Auditorium checks availability and rejects out-of-range seats', () => {
  const a = new Auditorium(1, '.....\n..A..\n');
  assert.equal(a.isAvailable(1, 0, 5), true);
  assert.equal(a.isAvailable(2, 1, 2), false);
  assert.equal(a.isAvailable(1, 4, 2), false, 'runs off the end of the row');
  assert.equal(a.isAvailable(3, 0, 1), false, 'row does not exist');
  assert.equal(a.isAvailable(1, 0, 0), false, 'zero seats');
});

test('findBestAvailable picks the block closest to center, lower row on ties', () => {
  // 5x5 grid, center seat (3, C) is taken
  const a = new Auditorium(1, '.....\n.....\n..A..\n.....\n.....\n');
  assert.deepEqual(a.findBestAvailable(1), { row: 2, seat: 2 });
  assert.deepEqual(a.findBestAvailable(5), { row: 2, seat: 0 });
  assert.equal(a.findBestAvailable(6), null);
  const full = new Auditorium(2, 'AA\n');
  assert.equal(full.findBestAvailable(1), null);
});
