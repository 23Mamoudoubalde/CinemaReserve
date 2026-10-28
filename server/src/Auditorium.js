// JavaScript port of Auditorium.java, Node.java and Seat.java.
// Seats live in a 2D linked list: each node points right (same row)
// and down (same seat, next row). Rows are 1-indexed, seats 0-indexed
// (seat 0 = "A"), matching the Java version.

import { readFileSync, writeFileSync } from 'node:fs';

class SeatNode {
  constructor(row, seat, ticketType) {
    this.row = row;
    this.seat = seat;
    this.ticketType = ticketType; // 'A' adult, 'C' child, 'S' senior, '.' open
    this.right = null;
    this.down = null;
  }

  get reserved() {
    return this.ticketType !== '.';
  }
}

export class Auditorium {
  constructor(id, layoutText) {
    this.id = id;
    const lines = layoutText.split(/\r?\n/).filter((l) => l.length > 0);
    this.numRows = lines.length;
    this.seatsPerRow = lines.length ? lines[0].length : 0;
    this.first = null;

    let previousRow = null;
    lines.forEach((line, r) => {
      let rowHead = null;
      let previousSeat = null;
      for (let s = 0; s < this.seatsPerRow; s++) {
        const node = new SeatNode(r + 1, s, line[s] ?? '.');
        if (!rowHead) rowHead = node;
        if (previousSeat) previousSeat.right = node;
        previousSeat = node;
      }
      if (!this.first) this.first = rowHead;
      // Link the previous row down to this one, seat by seat
      for (let up = previousRow, cur = rowHead; up && cur; up = up.right, cur = cur.right) {
        up.down = cur;
      }
      previousRow = rowHead;
    });
  }

  static fromFile(id, path) {
    return new Auditorium(id, readFileSync(path, 'utf8'));
  }

  #inBounds(row, seat) {
    return Number.isInteger(row) && Number.isInteger(seat) &&
      row >= 1 && row <= this.numRows && seat >= 0 && seat < this.seatsPerRow;
  }

  #node(row, seat) {
    let node = this.first;
    for (let i = 1; i < row; i++) node = node.down;
    for (let i = 0; i < seat; i++) node = node.right;
    return node;
  }

  *#rows() {
    for (let row = this.first; row; row = row.down) yield row;
  }

  *#allSeats() {
    for (const row of this.#rows()) {
      for (let node = row; node; node = node.right) yield node;
    }
  }

  isAvailable(row, startSeat, count) {
    if (!Number.isInteger(count) || count < 1) return false;
    if (!this.#inBounds(row, startSeat) || startSeat + count > this.seatsPerRow) return false;
    let node = this.#node(row, startSeat);
    for (let i = 0; i < count; i++, node = node.right) {
      if (node.reserved) return false;
    }
    return true;
  }

  reserve(row, seat, ticketType) {
    if (!this.#inBounds(row, seat)) return false;
    this.#node(row, seat).ticketType = ticketType;
    return true;
  }

  unreserve(row, seat) {
    if (!this.#inBounds(row, seat)) return false;
    this.#node(row, seat).ticketType = '.';
    return true;
  }

  ticketTypeAt(row, seat) {
    return this.#inBounds(row, seat) ? this.#node(row, seat).ticketType : '.';
  }

  // Best block of `count` consecutive open seats, closest to the center.
  // Ties go to the lower row, then the leftmost seat (same rule as Java).
  findBestAvailable(count) {
    if (!Number.isInteger(count) || count < 1 || count > this.seatsPerRow) return null;
    const centerRow = (this.numRows + 1) / 2;
    const centerSeat = (this.seatsPerRow + 1) / 2;
    let best = null;
    let bestDistance = Infinity;

    for (let r = 1; r <= this.numRows; r++) {
      for (let s = 0; s <= this.seatsPerRow - count; s++) {
        if (!this.isAvailable(r, s, count)) continue;
        const middle = s + (count - 1) / 2 + 1;
        const distance = Math.hypot(r - centerRow, middle - centerSeat);
        if (distance < bestDistance) {
          bestDistance = distance;
          best = { row: r, seat: s };
        }
      }
    }
    return best;
  }

  // Grid of booleans for the seat map (true = taken). Ticket types stay private.
  seatMap() {
    return [...this.#rows()].map((row) => {
      const out = [];
      for (let node = row; node; node = node.right) out.push(node.reserved);
      return out;
    });
  }

  stats() {
    const counts = { open: 0, reserved: 0, adult: 0, child: 0, senior: 0 };
    for (const node of this.#allSeats()) {
      if (!node.reserved) { counts.open++; continue; }
      counts.reserved++;
      if (node.ticketType === 'A') counts.adult++;
      if (node.ticketType === 'C') counts.child++;
      if (node.ticketType === 'S') counts.senior++;
    }
    return counts;
  }

  toText() {
    return [...this.#rows()].map((row) => {
      let line = '';
      for (let node = row; node; node = node.right) line += node.ticketType;
      return line;
    }).join('\n') + '\n';
  }

  saveToFile(path) {
    writeFileSync(path, this.toText());
  }
}
