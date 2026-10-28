CinemaReserve is a comprehensive Java-based ticket reservation system that simulates a real-world movie theater booking platform. It provides separate interfaces for customers and administrators and uses key object-oriented principles such as encapsulation, polymorphism, and inheritance. A HashMap is used to manage users and their orders, and seat availability is maintained using auditorium files.

## 🚀 Features

### 👥 Customer Functionality
- **User Login:** Secure login using `userdb.dat` credentials.
- **Reserve Seats:** Choose from 3 auditoriums and reserve multiple ticket types (Adult, Child, Senior).
- **Best Seat Finder:** Automatically suggests the best available seats if chosen seats are unavailable.
- **View Orders:** See all past and active orders with details.
- **Update Orders:** Add or remove seats, or cancel entire orders.
- **Display Receipt:** Shows itemized receipts for each order and the total.

### 🛠️ Admin Functionality
- **Admin Login:** Special access using "admin" credentials from `userdb.dat`.
- **Print Report:** Displays summary of open/reserved seats, ticket type counts, and revenue for each auditorium.
- **Exit System:** Saves updated seat layouts to final output files (`A1Final.txt`, `A2Final.txt`, `A3Final.txt`).

## 🧠 Technologies Used
- Java
- Java Collections (HashMap, LinkedList)
- File I/O (reading/writing seat data and user credentials)
- Exception handling
- Node.js, Express, and React for the web version (below)

---

## 🌐 Web Version (Node.js + React)

The original Java console app is still here. The `server/` and `client/` folders add a web version of the same system:

- **`server/`**: a Node.js/Express REST API. It ports the Java logic to JavaScript: the custom hash table (`Hashmap.js`, separate chaining with rehashing), the 2D linked list of seats and the best-seat finder (`Auditorium.js`), and the order/receipt/report rules (`Theater.js`).
- **`client/`**: a React (Vite) front end with a clickable seat map, ticket picker, order management, and an admin sales report.

### Run it locally

You need Node.js 20 or newer.

```bash
# Terminal 1: API on http://localhost:3001
cd server
npm install
npm run dev

# Terminal 2: React app on http://localhost:5173
cd client
npm install
npm run dev
```

For a single server, run `npm run build` in `client/`, then `npm start` in `server/`. Express then serves the built React app at http://localhost:3001.

Demo logins come from `server/data/userdb.dat`: `mamoudou / demo123`, `alex / test456`, and `admin / admin123`. These are demo passwords only. The server hashes them with scrypt when it loads them, so plain-text passwords never sit in memory.

### API

All routes except login need an `Authorization: Bearer <token>` header.

| Method | Route | What it does |
|---|---|---|
| POST | `/api/login` | Log in; returns a token. Locks the username for 1 minute after 3 failed tries |
| POST | `/api/logout` | End the session |
| GET | `/api/auditoriums` | List auditoriums with open-seat counts |
| GET | `/api/auditoriums/:id/seats` | Seat map (taken / open) |
| GET | `/api/auditoriums/:id/best?count=N` | Best block of N seats closest to center |
| GET | `/api/orders` | Your orders, seat lists, and totals |
| POST | `/api/orders` | Book `{ auditorium, row, seat, adult, child, senior }`. Returns 409 with `bestAvailable` if the seats are taken |
| POST | `/api/orders/:id/tickets` | Add tickets to an order |
| DELETE | `/api/orders/:id/seats/:row/:seat` | Remove one seat from an order |
| DELETE | `/api/orders/:id` | Cancel an order |
| GET | `/api/admin/report` | Admin: open/reserved seats, ticket types, and sales per auditorium |
| POST | `/api/admin/save` | Admin: write `A1Final.txt`–`A3Final.txt` |

Rows start at 1 and seats start at 0 (seat 0 = "A"), the same as the Java version. Prices: Adult $10.00, Child $5.00, Senior $7.50.

### Tests

```bash
cd server
npm test
```

Unit tests cover the hash table and seat logic. API tests cover login, lockout, booking, conflicts, add/remove/cancel, input validation, access control, and the admin report. They run against a temporary copy of the data files.
