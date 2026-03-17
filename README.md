# The-Super-Event-booking-system
This is a  desktop ticket‑booking application built using Java, JavaFX, MVC architecture, and SQLite. The system supports user signup/login, event browsing, seat booking with availability validation, checkout with confirmation code, order history, and data persistence. Includes optional admin features such as event management and event disabling.


## 📌 Overview
The Super Event Booking System is a GUI-based ticketing application that allows users to browse events, book seats, manage a shopping cart, and complete checkout with validation.  
The system follows the MVC design pattern, uses JDBC with SQLite for data storage, and implements additional OO design principles.

---

## ✨ Features

### 👤 User Features
- **User Signup & Login**  
  Create an account with username, password, and preferred name.

- **Dashboard**  
  Personalized welcome message and full event list loaded from the database.

- **Event Booking**  
  - Add events to a shopping cart  
  - Modify quantities or remove items  
  - Automatic seat availability validation  
  - Prevents overbooking and handles edge cases

- **Checkout Process**  
  - Validates a **6‑digit confirmation code**  
  - Ensures events are bookable based on the current day (Mon–Sun window)  
  - Updates sold tickets in the database upon successful checkout

- **Order History**  
  - Displays all past orders  
  - Shows order number, timestamp, items, and total price  
  - Sorted in reverse chronological order

- **Export Orders**  
  Export all orders to a text file with a user‑chosen filename and location.

- **Persistent Data**  
  All event, user, and order data is stored in SQLite and preserved across sessions.

---

## 🛠️ Admin Features (Advanced)
If logged in as the admin (`admin / Admin321`):

- **Admin Dashboard**
- **View all events**, grouped by title (no duplicates)
- **Enable/Disable events**  
  Disabled events are hidden from normal users but preserved in the database.
- **Event Management**
  - Add new events  
  - Delete events  
  - Modify venue, day, price, or capacity  
  - Duplicate checks included
- **View all user orders**

---

## 🔐 Additional User Functionality (Advanced)
- Users can update their password  
- Passwords are stored in encrypted form (simple character shifting)

---

## 🧪 JUnit Tests
Includes at least 5 JUnit test cases covering:
- Password validation  
- Seat availability checks  
- Existence checks  
- Other core validation logic

---

## 🧱 Technologies Used
- **Java 17+**
- **JavaFX**
- **SQLite (via JDBC)**
- **MVC Architecture**
- **Object-Oriented Design Patterns**

---

## ▶️ Running the Application

1. Ensure JavaFX is configured in your IDE (IntelliJ/Eclipse).
2. Ensure SQLite JDBC driver is included.
3. Run the main class:
