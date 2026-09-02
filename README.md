# 🦷 Sunrise Dental Clinic Management System

[![CI/CD Pipeline](https://github.com/MalithShehan/BSCSE_CIS6003_20374225/actions/workflows/ci.yml/badge.svg)](https://github.com/MalithShehan/BSCSE_CIS6003_20374225/actions)
[![Java Version](https://img.shields.io/badge/Java-17%20LTS-blue.svg)](https://www.oracle.com/java/)
[![Servlet API](https://img.shields.io/badge/Jakarta%20Servlet-6.0-orange.svg)](https://jakarta.ee/)
[![Server](https://img.shields.io/badge/Apache%20Tomcat-10.1-green.svg)](https://tomcat.apache.org/)
[![Database](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Academic%20CIS6003-purple.svg)]()

> A robust, distributed 3-tier clinical management and patient reservation web application engineered for **Sunrise Dental Clinic, Colombo**, fulfilling all assessment criteria for **Cardiff Metropolitan University — CIS6003 Advanced Programming**.

---

## 📋 Table of Contents
1. [Project Overview & Scenario](#-project-overview--scenario)
2. [Key System Features](#-key-system-features)
3. [Technology Stack](#-technology-stack)
4. [3-Tier Distributed Architecture](#-3-tier-distributed-architecture)
5. [Database Architecture & Advanced Features](#-database-architecture--advanced-features)
6. [Design Patterns Applied](#-design-patterns-applied)
7. [REST API Endpoint Catalog](#-rest-api-endpoint-catalog)
8. [Demo Test Users](#-demo-test-users)
9. [Database Setup Guide](#-database-setup-guide)
10. [Building & Deployment Instructions](#-building--deployment-instructions)
11. [Testing & Quality Assurance](#-testing--quality-assurance)
12. [CI/CD Workflow & GitHub Actions](#-cicd-workflow--github-actions)
13. [Screenshots & UI Showcase](#-screenshots--ui-showcase)

---

## 🏥 Project Overview & Scenario

**Sunrise Dental Clinic** is a busy private dental center located in Colombo, Sri Lanka, providing specialized dental care to hundreds of patients weekly. Previously, the clinic relied on manual paper books and index cards, resulting in:
- Unintended double-bookings and dentist scheduling collisions.
- Misplaced patient history cards and communication delays.
- Human calculation errors in billing and discount deductions.
- Lack of executive visibility into clinic revenue and dentist workloads.

This project delivers a **modern, computerized 3-tier distributed web application** that completely digitizes clinic operations from patient intake to automated sequential appointment generation (`SDC-YYYY-XXXX`), procedural billing, receipt printing, and decision-support analytics.

---

## ✨ Key System Features

- **Role-Based Access Control (RBAC)**: Distinct permissions for `ADMIN`, `RECEPTIONIST`, and `DENTIST`.
- **BCrypt Password Security**: Salted, constant-time password hashing ($12$ rounds).
- **Automated Sequential Identification**: Generates unique appointment numbers (e.g. `SDC-2026-0001`).
- **Database Trigger Double-Booking Prevention**: Database-level triggers prevent dentist schedule collisions.
- **Dynamic Fee Computation**: Formula: $\text{Total Bill} = \text{Dentist Consultation Fee} + \text{Treatment Cost} - \text{Discount Amount}$.
- **Stored Procedure Invoicing**: Atomically generates invoices (`INV-YYYY-XXXX`), transitions appointment status to `COMPLETED`, and enforces billing idempotency.
- **Printable Patient Receipts**: Dedicated print stylesheet (`@media print`) for clean paper/PDF receipts.
- **Simulated Multi-Channel Notifications**: Observer/Strategy pattern sending simulated SMS and Email notifications upon booking and invoice settlement.
- **Executive Decision-Support Views**:
  - `Daily_Appointment_Report`: Daily workload and completion rate.
  - `Monthly_Revenue_Report`: Financial breakdown (consultation, treatment, discounts, net revenue).
  - `Dentist_Performance_Report`: Patient retention and revenue attribution per doctor.

---

## 🛠 Technology Stack

### Presentation Tier (Client)
- **HTML5**: Semantic web architecture with accessible form structures.
- **Vanilla CSS3**: Responsive design system, Plus Jakarta Sans typography, medical teal/cyan dental theme, and print styles. *(Zero external CSS frameworks)*.
- **Vanilla JavaScript (ES6+)**: Asynchronous `fetch()` API client communicating via REST JSON endpoints.

### Business Logic Tier (Server)
- **Java 17 LTS**: Strict object-oriented design and business logic encapsulation.
- **Jakarta Servlet API 6.0**: Controller servlets hosted on **Apache Tomcat 10.1+**.
- **Google Gson 2.10.1**: Clean JSON streaming and custom SQL Date/Time/Timestamp serializers.
- **jBCrypt 0.4**: Salted cryptographic password hashing.
- **JUnit 5 & Mockito 5**: Unit and mock test suites.

### Data Tier (Persistence)
- **MySQL 8.0+ Community Server**: Relational schema with foreign keys, checks, unique constraints, and indexes.
- **Advanced Database Objects**: Stored Functions, Stored Procedures, Triggers, and Views.
- **JDBC (MySQL Connector/J 8.3.0)**: PreparedStatement-based DAO layer.

---

## 🏛 3-Tier Distributed Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION TIER                              │
│   HTML5 / CSS3 / Vanilla JavaScript Fetch API (Client Web Browser)       │
│   - login.html               - dashboard.html    - register-appointment.html │
│   - search-appointment.html  - bill.html         - reports.html              │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ JSON over HTTP (REST)
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         BUSINESS LOGIC TIER                              │
│   Apache Tomcat 10.1+ Web Container (Jakarta EE 10)                      │
│   ├── Security: AuthFilter (Session Guard & CORS)                        │
│   ├── Controllers: LoginServlet, AppointmentServlet, BillServlet, etc.   │
│   ├── Services: AuthService, AppointmentService, BillingService, etc.    │
│   └── DAOs: UserDAO, AppointmentDAO, InvoiceDAO, ReportDAO               │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ Parameterized JDBC (PreparedStatement)
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                              DATA TIER                                   │
│   MySQL 8.0 Relational Database (sunrise_dental_db)                      │
│   ├── Tables: users, patients, dentists, treatments, appointments, etc.  │
│   ├── Procedures: GenerateBillForAppointment                             │
│   ├── Functions: GetAppointmentTotal                                     │
│   ├── Triggers: prevent_double_booking, log_appointment_status_change    │
│   └── Views: Daily_Appointment_Report, Monthly_Revenue_Report, etc.      │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 🗄 Database Architecture & Advanced Features

### Relational Tables
1. `users` — Staff user accounts, roles (`ADMIN`, `RECEPTIONIST`, `DENTIST`), and BCrypt hashes.
2. `patients` — Patient demographics and contact numbers.
3. `dentists` — Specialists, specializations, and consultation fees.
4. `treatments` — Catalog of dental procedures and standard costs.
5. `appointments` — Appointment schedule and status (`SCHEDULED`, `COMPLETED`, `CANCELLED`).
6. `invoices` — Financial settlement records (`INV-YYYY-XXXX`).
7. `appointment_status_log` — State transition audit trail.

### Stored Routines & Triggers
- **Stored Function**: `GetAppointmentTotal(appointment_id)` — Returns consultation fee + treatment cost.
- **Stored Procedure**: `GenerateBillForAppointment(appointment_id, discount_pct, payment_method, OUT invoice_id, OUT invoice_num, OUT total_amt)` — Enforces cancellation checks and billing idempotency.
- **Triggers**:
  - `prevent_double_booking_insert` & `prevent_double_booking_update` — Blocks overlapping appointments for the same dentist.
  - `log_appointment_insert` & `log_appointment_status_change` — Maintains audit log in `appointment_status_log`.

---

## 🧩 Design Patterns Applied

| Pattern | Implementation Location | Architectural Benefit |
|---|---|---|
| **Model-View-Controller (MVC)** | HTML/JS (View), Servlets (Controller), Models/Services (Model) | Clean separation of UI, business orchestration, and data. |
| **Data Access Object (DAO)** | `UserDAO`, `AppointmentDAO`, `InvoiceDAO`, `ReportDAO` | Completely isolates raw SQL from service logic. |
| **Singleton Pattern** | `DatabaseConnection.java` | Thread-safe double-checked locking connection manager. |
| **Factory Method Pattern** | `NotificationFactory.java` | Decouples notification channel creation (`EMAIL` vs `SMS`). |
| **Observer / Strategy Pattern** | `NotificationService.java` | Dispatches simulated patient alerts asynchronously. |

---

## 📡 REST API Endpoint Catalog

All responses return standard JSON envelope: `{ "success": true, "message": "...", "data": {} }`.

| Method | Endpoint | Description | Access Role |
|---|---|---|---|
| `POST` | `/api/login` | Authenticates username/password and creates session | Public |
| `POST` | `/api/logout` | Terminates active session | Authenticated |
| `GET` | `/api/session` | Inspects current user authentication state | Authenticated |
| `GET` | `/api/dentists` | Retrieves active dentists & consultation fees | Authenticated |
| `GET` | `/api/treatments` | Retrieves dental procedures catalog | Authenticated |
| `POST` | `/api/appointments` | Books new appointment (auto-generates `SDC-YYYY-XXXX`) | Receptionist, Admin |
| `GET` | `/api/appointments` | Lists appointments (supports `appointmentNumber`, `date`, `dentistId`, `status`) | Authenticated |
| `PUT` | `/api/appointments` | Updates status (`COMPLETED`, `CANCELLED`) | Receptionist, Dentist, Admin |
| `DELETE` | `/api/appointments?id={id}` | Cancels appointment | Receptionist, Admin |
| `POST` | `/api/bill` | Generates official invoice via Stored Procedure | Receptionist, Admin |
| `GET` | `/api/bill` | Retrieves invoice receipt for printing (`?appointmentNumber={num}`) | Authenticated |
| `GET` | `/api/reports` | Retrieves analytical report data (`?type=daily\|monthly\|dentist\|summary`) | Admin |

---

## 👥 Demo Test Users

| Role | Username | Plain Password | Hashed Storage |
|---|---|---|---|
| **Director / Admin** | `admin` | `Admin@123` | `$2a$12$e8Yk1m3YqL4aO5X9o7VHQeG6vD5r8x8P1gM7lM1lB7xY.9X0K3Wym` |
| **Front-Desk Receptionist** | `receptionist` | `Reception@123` | `$2a$12$NqB8gZkFpG0/iIomJt1M5eKjO4z7mQ8bH4h2G2LqB9j8sD2tYmH.S` |
| **Dental Surgeon** | `dentist` | `Dentist@123` | `$2a$12$R9Z0x1y2z3a4b5c6d7e8f.O4j7M9c8N8n4G2LqB9j8sD2tYmH.S` |

---

## 🚀 Database Setup Guide

1. Ensure **MySQL Server 8.0+** is running locally on port `3306`.
2. Import the complete database script using PowerShell:
   ```powershell
   Get-Content "d:\Campus\BSCSE_CIS6003_20374225\database\sunrise_dental_clinic.sql" | mysql -u root -p
   ```
   *Or inside MySQL Command-Line Client:*
   ```sql
   source d:/Campus/BSCSE_CIS6003_20374225/database/sunrise_dental_clinic.sql;
   ```
3. Update connection credentials in `src/main/resources/db.properties` if your MySQL password differs:
   ```properties
   db.url=jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
   db.user=root
   db.password=YourPasswordHere
   ```

---

## 📦 Building & Deployment Instructions

### 1. Compile and Package with Maven
```bash
# Clean, compile, execute unit tests, and package WAR
mvn clean package
```
This produces `target/sunrise-dental-clinic.war`.

### 2. Deploy to Apache Tomcat 10.1
1. Copy `target/sunrise-dental-clinic.war` into your Tomcat `webapps/` folder:
   ```bash
   cp target/sunrise-dental-clinic.war /path/to/apache-tomcat-10.1.x/webapps/
   ```
2. Start Tomcat (`bin/startup.sh` or `bin/startup.bat`).
3. Open your browser and navigate to:
   ```
   http://localhost:8080/sunrise-dental-clinic/
   ```

---

## 🧪 Testing & Quality Assurance

### Run Unit & Mock Tests
```bash
mvn clean test
```

### Test Coverage Highlights
- `ValidationUtilTest`: Phone regexes, clinic hours ($08:00\text{--}17:00$), date ranges, discount limits.
- `PasswordUtilTest`: BCrypt 12 rounds verification, salt uniqueness.
- `AppointmentNumberGeneratorTest`: Sequence formatting `SDC-YYYY-XXXX`.
- `AuthServiceTest`: Credentials matching, invalid password rejection, inactive accounts.
- `AppointmentServiceTest`: Double-booking conflict detection, past date rejection.
- `BillingServiceTest`: Duplicate billing prevention, cancelled appointment blocking, discount boundaries.

---

## 🔄 CI/CD Workflow & GitHub Actions

The repository includes a GitHub Actions workflow in `.github/workflows/ci.yml`:
- **Triggers**: On every `push` and `pull_request` to `main` and `develop`.
- **JDK 17 Setup**: Uses Eclipse Temurin with automated Maven caching.
- **Continuous Integration**: Executes `mvn clean test` and validates builds.
- **Continuous Delivery**: Packages `sunrise-dental-clinic.war` and archives it as a downloadable release artifact.

---

## 📸 Screenshots & UI Showcase

*(Replace placeholders with system screenshots for final academic submission)*

| Login Portal | Dashboard & KPI Widgets |
|:---:|:---:|
| `[ Screenshot: login.html ]` | `[ Screenshot: dashboard.html ]` |

| Register Appointment & Live Cost Preview | Search & Manage Appointments |
|:---:|:---:|
| `[ Screenshot: register-appointment.html ]` | `[ Screenshot: search-appointment.html ]` |

| Interactive Billing & Printable Receipt | Executive Decision-Support Reports |
|:---:|:---:|
| `[ Screenshot: bill.html ]` | `[ Screenshot: reports.html ]` |

---

## 📄 Academic Submission Details
- **Module**: CIS6003 Advanced Programming
- **Institution**: Cardiff Metropolitan University
- **Deliverables**: Source Code, MySQL Scripts, PlantUML Models, Test Plan & RTM, CI/CD Pipeline, 4,000-Word Academic Report.
