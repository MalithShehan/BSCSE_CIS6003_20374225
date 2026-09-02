# Sunrise Dental Clinic — Test Plan, Traceability Matrix & Automation Strategy

**Module:** CIS6003 Advanced Programming  
**Target:** 100% Assessment Criteria Fulfillment (LO II & LO III)

---

## 1. Test Strategy & Test-Driven Development (TDD) Rationale

To ensure software robustness, data integrity, and compliance with clinical healthcare standards, a multi-tier testing strategy was implemented following the **Test-Driven Development (TDD)** lifecycle:

```
+-------------------------------------------------------------+
|                     TDD Red-Green-Refactor                  |
|                                                             |
|   1. Write Failing Test  -->  2. Implement Code  -->  3. Refactor  |
|      (JUnit 5 / Mockito)       (Service / DAO)         (Clean Code)|
+-------------------------------------------------------------+
```

### Test Levels
1. **Unit Testing (JUnit 5)**: Validates isolated functions (regex formatting, BCrypt cryptography, sequence generators, boundary limits).
2. **Mock & Service Testing (Mockito 5)**: Validates business logic and exception scenarios without requiring a live database instance.
3. **Integration Testing**: Verifies database constraints, triggers, stored procedures, and JDBC connections.
4. **End-to-End UI Automation (Selenium WebDriver)**: Validates user workflows from browser login to invoice printing.

---

## 2. Test Plan & Scope

| Test Scope | Objectives | Tools / Frameworks |
|---|---|---|
| **Input Validation** | Validate phone numbers, clinic hours ($08:00\text{--}17:00$), past dates, appointment numbers. | JUnit 5 (`ValidationUtilTest`) |
| **Authentication & Cryptography** | Validate BCrypt work factor 12, salt uniqueness, password verification, inactive accounts. | JUnit 5 & Mockito (`PasswordUtilTest`, `AuthServiceTest`) |
| **Appointment Scheduling** | Validate booking creation, double-booking rejection, sequential formatting `SDC-YYYY-XXXX`. | JUnit 5 & Mockito (`AppointmentServiceTest`, `AppointmentNumberGeneratorTest`) |
| **Financial Billing** | Validate idempotency (no duplicate bills), discount calculation ($0\text{--}100\%$), Stored Procedure execution. | JUnit 5 & Mockito (`BillingServiceTest`) |
| **UI Workflows** | Verify login, booking, searching, billing, and receipt generation in web browser. | Selenium WebDriver (`SeleniumE2ETest`) |

---

## 3. Test Data Tables

### Table 3.1: Staff Authentication Test Data
| Test Case ID | Input Username | Input Password | Expected Role | Expected Result |
|---|---|---|---|---|
| `AUTH-01` | `admin` | `Admin@123` | `ADMIN` | **PASS**: 200 OK, Session created |
| `AUTH-02` | `receptionist` | `Reception@123` | `RECEPTIONIST` | **PASS**: 200 OK, Session created |
| `AUTH-03` | `dentist` | `Dentist@123` | `DENTIST` | **PASS**: 200 OK, Session created |
| `AUTH-04` | `admin` | `WrongPass@123` | N/A | **FAIL (Expected)**: 401 Unauthorized |
| `AUTH-05` | `unknown_user` | `Password@123` | N/A | **FAIL (Expected)**: 401 Unauthorized |
| `AUTH-06` | `""` (Empty) | `""` (Empty) | N/A | **FAIL (Expected)**: 400 Bad Request |

### Table 3.2: Appointment Booking & Scheduling Test Data
| Test Case ID | Patient Name | Phone Number | Attending Dentist | Date | Time | Expected Result |
|---|---|---|---|---|---|---|
| `BOOK-01` | Kasun Dias | `0779998888` | Dr. Ruwan Silva (ID: 1) | Future Date ($+2$ days) | `10:00` | **PASS**: `SDC-2026-0001` created |
| `BOOK-02` | Chamari A. | `0712345678` | Dr. Ruwan Silva (ID: 1) | Future Date ($+2$ days) | `10:00` | **FAIL (Expected)**: Double-booking Conflict (409) |
| `BOOK-03` | Nimal Perera | `invalid_phone` | Dr. Nimal S. (ID: 2) | Future Date ($+1$ day) | `14:00` | **FAIL (Expected)**: 400 Bad Request (Invalid phone) |
| `BOOK-04` | Anusha W. | `0754567890` | Dr. Dilani J. (ID: 3) | Past Date ($-1$ day) | `09:30` | **FAIL (Expected)**: 400 Bad Request (Past date) |
| `BOOK-05` | Dinesh C. | `0763456789` | Dr. Ruwan Silva (ID: 1) | Future Date ($+1$ day) | `19:00` | **FAIL (Expected)**: 400 Bad Request (Outside clinic hours) |

### Table 3.3: Billing & Discount Test Data
| Test Case ID | Appointment Ref | Consultation Fee | Treatment Cost | Discount % | Expected Total | Expected Result |
|---|---|---|---|---|---|---|
| `BILL-01` | `SDC-2026-0001` | LKR 2,500.00 | LKR 4,500.00 | $10.0\%$ | LKR 6,300.00 | **PASS**: `INV-2026-0001` generated |
| `BILL-02` | `SDC-2026-0001` | LKR 2,500.00 | LKR 4,500.00 | $10.0\%$ | N/A | **FAIL (Expected)**: 409 Conflict (Already billed) |
| `BILL-03` | `SDC-2026-0004` (Cancelled) | LKR 3,000.00 | LKR 18,000.00 | $0.0\%$ | N/A | **FAIL (Expected)**: 409 Conflict (Cancelled) |
| `BILL-04` | `SDC-2026-0002` | LKR 3,500.00 | LKR 45,000.00 | $120.0\%$ | N/A | **FAIL (Expected)**: 400 Bad Request (Discount $> 100\%$) |

---

## 4. Requirement Traceability Matrix (RTM)

| Assessment Requirement ID | Description | Design Artifact / Source Code | Test Class & Automated Test Cases | Status |
|---|---|---|---|---|
| **REQ-01** | User Authentication & RBAC | `UserDAO.java`, `AuthService.java`, `LoginServlet.java`, `AuthFilter.java` | `AuthServiceTest#testAuthenticate_Success`, `testAuthenticate_WrongPassword`, `testAuthenticate_InactiveUser` | **VERIFIED** |
| **REQ-02** | Register New Appointment | `AppointmentDAO.java`, `AppointmentService.java`, `AppointmentServlet.java` | `AppointmentServiceTest#testBookAppointment_Success`, `testBookAppointment_DoubleBookingConflict` | **VERIFIED** |
| **REQ-03** | Display & Search by Appointment No | `AppointmentDAO.java`, `AppointmentServlet.java`, `search-appointment.html` | `AppointmentServiceTest#testGetAppointmentByNumber_Success`, `ValidationUtilTest#testIsValidAppointmentNumber` | **VERIFIED** |
| **REQ-04** | Calculate & Print Patient Bill | `InvoiceDAO.java`, `BillingService.java`, `BillServlet.java`, `bill.html` | `BillingServiceTest#testGenerateBill_Success`, `testGenerateBill_PreventDuplicateBilling` | **VERIFIED** |
| **REQ-05** | Double-Booking Prevention | MySQL Triggers `prevent_double_booking_insert/update`, `AppointmentService.java` | `AppointmentServiceTest#testBookAppointment_DoubleBookingConflict` | **VERIFIED** |
| **REQ-06** | Hashed Password Storage | `PasswordUtil.java` (BCrypt 12 rounds) | `PasswordUtilTest#testHashPassword_Format`, `testHashPassword_SaltUniqueness`, `testVerifyPassword_Success` | **VERIFIED** |
| **REQ-07** | Input & Boundary Validations | `ValidationUtil.java`, `validation.js` | `ValidationUtilTest#testIsValidPhoneNumber_ValidFormats`, `testIsWithinClinicHours_Valid` | **VERIFIED** |
| **REQ-08** | Decision-Support Reports | `ReportDAO.java`, `ReportService.java`, `ReportServlet.java`, `reports.html` | Integrated View Tests (`Daily_Appointment_Report`, `Monthly_Revenue_Report`, `Dentist_Performance_Report`) | **VERIFIED** |
| **REQ-09** | Simulated Notification Dispatch | `NotificationService.java`, `NotificationFactory.java` | `EmailNotificationService`, `SmsNotificationService` mock verifications | **VERIFIED** |

---

## 5. Instructions for Running Tests

### Option A: Running Automated Tests via Command Line (Maven)
Execute the Maven Surefire test lifecycle from the project root:
```bash
mvn clean test
```

To run a specific test suite:
```bash
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=AppointmentServiceTest
mvn test -Dtest=BillingServiceTest
```

### Option B: Generating Surefire HTML Test Reports
```bash
mvn surefire-report:report
```
HTML test summary will be generated in `target/site/surefire-report.html`.

### Option C: Automated CI/CD Execution
Tests are automatically triggered upon every `push` and `pull_request` to the GitHub repository via GitHub Actions (`.github/workflows/ci.yml`).
