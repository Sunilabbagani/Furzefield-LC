# Fitness Lesson Booking System

## 📌 Overview

The **Fitness Lesson Booking System (FLC System)** is a Java-based console application designed to manage fitness classes, members, and bookings efficiently.
It allows users to register members, schedule lessons, make bookings, and manage reviews in a structured way.

This project demonstrates object-oriented programming principles, modular design, and basic system management functionality.

---

## 🚀 Features

* 👤 Member registration and management
* 📅 Lesson scheduling with timetable support
* 📝 Booking system with status tracking (e.g., booked, cancelled)
* ⭐ Review system for lessons
* 🧠 Central system manager to control operations
* 💻 Interactive console-based user interface

---

## 🏗️ Project Structure

```bash
.
├── Main.java                # Entry point of the application
├── SystemManager.java      # Core system logic and coordination
├── SmartConsoleUI.java     # Console-based user interface
├── DataInitializer.java    # Loads sample data for testing
├── Booking.java            # Booking entity
├── BookingStatus.java      # Enum for booking states
├── Member.java             # Member entity
├── Lesson.java             # Lesson entity
├── Review.java             # Review entity
├── Timetable.java          # Manages lesson schedules
├── TimeSlot.java           # Represents time slots
├── Day.java                # Enum for days of the week
```

---

## 🧠 Concepts Used

* Object-Oriented Programming (OOP)

  * Encapsulation
  * Abstraction
  * Modularity
* Collections (Lists, Maps)
* Enum usage for fixed values
* Separation of concerns (UI vs Logic)

---

## ⚙️ How to Run

### 1. Clone the repository

```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
```

### 2. Compile the project

```bash
javac *.java
```

### 3. Run the application

```bash
java Main
```

---

## 🧪 Sample Functionality

* Add a new member
* View available lessons
* Book a lesson
* Cancel a booking
* Add and view reviews

---

## 📈 Future Improvements

* GUI interface (JavaFX or Swing)
* Database integration (MySQL / SQLite)
* User authentication system
* Advanced reporting and analytics

---

## 🐞 Known Issues

* Console UI may not handle invalid input robustly
* No persistent data storage (data resets on restart)

---

## 👤 Author

**Sunilabbagani**

---

## 📜 License

This project is for academic purposes only.
