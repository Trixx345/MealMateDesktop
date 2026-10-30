# MealMateDesktop 🍽️

A Java Swing-based Meal Booking and Management System backed by a MySQL database.

## 📌 About the Project

MealMateDesktop is a desktop application designed to simplify meal booking and management for students. It provides a graphical interface for users to log in, register, browse meals, book meals, and manage their bookings.

The application uses Java for its frontend and application logic, JDBC for database connectivity, and MySQL for storing and managing data.

## 🛠️ Technologies Used

- **Programming Language:** Java
- **Frontend:** Java Swing
- **Database:** MySQL
- **Database Connectivity:** JDBC
- **IDE:** Any Java-compatible IDE
- **Version Control:** Git and GitHub

## ✨ Features

- Student registration and login
- Password hashing and authentication
- Meal booking system
- Booking quantity management
- Booking cancellation
- Booking collection status management
- Student booking history
- Wallet and payment management
- Database-driven application

## 🗄️ Database

The project uses MySQL as its database.

Database name: `meal_booking_db`

Main database tables include:

- `mbs_users` – Stores user login and authentication information.
- `mbs_students` – Stores student details.
- `mbs_bookings` – Stores meal booking information.
- `mbs_payments` – Stores payment and wallet transaction information.

The database export is available in:

`database/mealmate.sql`

## 📂 Project Structure

```text
MealMateDesktop/
│
├── src/
│   └── com/
│       └── mealmate/
│           ├── Main.java
│           ├── dao/
│           ├── model/
│           ├── service/
│           ├── ui/
│           └── util/
│
├── database/
│   └── mealmate.sql
│
├── config.properties
├── .gitignore
└── README.md
```

## ⚙️ Requirements

- Java JDK
- My
