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
