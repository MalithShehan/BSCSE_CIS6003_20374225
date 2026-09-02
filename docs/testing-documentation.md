# 🧪 Automated Testing Documentation & TDD Evaluation

**Module:** CIS6003 Advanced Programming  
**Target:** Excellent 70–100% Marking Band (LO II: 20 Marks)

---

## 1. Test-Driven Development (TDD) Methodology & Rationale

Software engineering within healthcare data ecosystems demands strict accuracy, deterministic behavior, and data preservation. To eliminate regressions and enforce business logic at every stage of development, this project implemented **Test-Driven Development (TDD)** using **JUnit 5** and **Mockito 5**.

### The TDD Cycle Implemented
1. **Red Stage**: An automated unit test was authored defining required behavior (e.g., verifying that `AppointmentService` throws `IllegalStateException` on a double-booking conflict).
2. **Green Stage**: The minimum requisite code was implemented in the service and DAO layer to satisfy the test condition.
3. **Refactor Stage**: The code was cleaned, decoupled using design patterns (DAO, Singleton, Factory), and optimized with parameterized `PreparedStatement` queries.

---

## 2. Comprehensive Test Suite Architecture

```
src/test/java/
├── util/
│   ├── ValidationUtilTest.java             # Regex, clinic hour boundary, date checks
│   ├── PasswordUtilTest.java               # BCrypt work factor 12, salt uniqueness
│   └── AppointmentNumberGeneratorTest.java  # SDC-YYYY-XXXX sequence formatting
├── service/
│   ├── AuthServiceTest.java                # BCrypt auth, inactive user checks (Mockito)
│   ├── AppointmentServiceTest.java         # Booking, double-booking rejection (Mockito)
│   └── BillingServiceTest.java             # Duplicate billing prevention, discounts (Mockito)
└── ui/
    └── SeleniumE2ETest.java                # Browser acceptance testing (Selenium)
```

---

## 3. Test Cases & Execution Results

### 3.1 Utility & Cryptographic Tests

| Test Identifier | Test Target | Input / Condition | Expected Output | Actual Result |
|---|---|---|---|---|
| `UT-VAL-01` | Phone Validation | `0771234567`, `+94771234567` | `true` | **PASS** |
| `UT-VAL-02` | Phone Validation | `12345`, `letters_only` | `false` | **PASS** |
| `UT-VAL-03` | Operating Hours | `08:00`, `12:30`, `17:00` | `true` | **PASS** |
| `UT-VAL-04` | Operating Hours | `07:59`, `17:01`, `22:00` | `false` | **PASS** |
| `UT-VAL-05` | Date Validation | Past Date (Yesterday) | `false` | **PASS** |
| `UT-VAL-06` | App Number Format | `SDC-2026-0001` | `true` | **PASS** |
| `UT-VAL-07` | Discount Limits | `0.0`, `50.0`, `100.0` | `true` | **PASS** |
| `UT-VAL-08` | Discount Limits | `-5.0`, `105.0` | `false` | **PASS** |
| `UT-SEC-01` | BCrypt Hash Format | `Admin@123` | Length 60, starts with `$2a$12$` | **PASS** |
| `UT-SEC-02` | Salt Randomness | Same password hashed twice | `hash1 != hash2` | **PASS** |
| `UT-SEC-03` | Hash Verification | Matching candidate password | `true` | **PASS** |
| `UT-SEC-04` | Hash Rejection | Incorrect password | `false` | **PASS** |
| `UT-GEN-01` | Number Generation | Year: 2026, Seq: 1 | `"SDC-2026-0001"` | **PASS** |

### 3.2 Service Layer Mock Tests (Mockito)

| Test Identifier | Test Target | Scenario / Mock Behavior | Assertion / Verification | Actual Result |
|---|---|---|---|---|
| `ST-AUTH-01` | `AuthService` | Valid username & password | User returned with role `RECEPTIONIST` | **PASS** |
| `ST-AUTH-02` | `AuthService` | Unknown username | Throws `SecurityException` | **PASS** |
| `ST-AUTH-03` | `AuthService` | Wrong password | Throws `SecurityException` | **PASS** |
| `ST-AUTH-04` | `AuthService` | Inactive user account | Throws `SecurityException("inactive")` | **PASS** |
| `ST-APP-01` | `AppointmentService`| Available dentist & valid time | Creates appointment with `SDC-2026-0001` | **PASS** |
| `ST-APP-02` | `AppointmentService`| Double-booking detected | Throws `IllegalStateException("Conflict")` | **PASS** |
| `ST-APP-03` | `AppointmentService`| Past appointment date | Throws `IllegalArgumentException` | **PASS** |
| `ST-APP-04` | `AppointmentService`| Time outside 08:00–17:00 | Throws `IllegalArgumentException` | **PASS** |
| `ST-BILL-01` | `BillingService` | Valid appointment & 10% discount | Returns invoice `INV-2026-0001` with net total | **PASS** |
| `ST-BILL-02` | `BillingService` | Appointment already invoiced | Throws `IllegalStateException("already generated")` | **PASS** |
| `ST-BILL-03` | `BillingService` | Cancelled appointment billing | Throws `IllegalStateException("cancelled")` | **PASS** |
| `ST-BILL-04` | `BillingService` | Discount $> 100\%$ | Throws `IllegalArgumentException` | **PASS** |

---

## 4. Requirements Traceability Matrix (RTM)

```
+---------------------------------------------------------------------------------------------------------+
| Assessment Req  | Functionality / Business Rule  | Implementing Class | Automated Test Method  | Status |
+=========================================================================================================+
| REQ-01          | User Authentication & RBAC     | AuthService.java   | AuthServiceTest#01-04  | VERIFIED|
| REQ-02          | Register Appointment           | AppointmentService | AppServiceTest#01-04   | VERIFIED|
| REQ-03          | Search by Appointment Number   | AppointmentDAO     | AppServiceTest#05      | VERIFIED|
| REQ-04          | Calculate and Print Bill       | BillingService     | BillingServiceTest#01  | VERIFIED|
| REQ-05          | Double-Booking Prevention      | DB Trigger & Svc   | AppServiceTest#02      | VERIFIED|
| REQ-06          | BCrypt Password Hashing        | PasswordUtil       | PasswordUtilTest#01-04 | VERIFIED|
| REQ-07          | Clinic Hours Validation        | ValidationUtil     | ValidationUtilTest#03  | VERIFIED|
| REQ-08          | Stored Procedure Invoicing     | InvoiceDAO         | BillingServiceTest#01  | VERIFIED|
| REQ-09          | Decision-Support Reports       | ReportDAO          | ReportService & Views  | VERIFIED|
+---------------------------------------------------------------------------------------------------------+
```

---

## 5. End-to-End Test Automation with Selenium WebDriver

To validate end-user acceptance, browser interactions were automated using Selenium WebDriver (`SeleniumE2ETest.java`):

1. **User Login Test**: Authenticates with `receptionist` / `Reception@123` and verifies browser redirection to `dashboard.html`.
2. **Negative Authentication Test**: Submits invalid credentials and verifies display of `.alert-danger`.
3. **Appointment Scheduling Workflow**: Fills out `register-appointment.html`, triggers live calculation, submits the form, and asserts receipt of `.alert-success` with reference `SDC-2026-XXXX`.
4. **Search Workflow**: Queries `search-appointment.html?search=SDC-2026-0001` and asserts visibility of patient details card.
5. **Billing & Print Receipt**: Enters discount on `bill.html`, clicks *Issue Official Invoice*, and validates receipt layout.

---

## 6. Maven Test Automation & CI/CD Instructions

```bash
# Execute entire test suite
mvn clean test

# Execute single test class
mvn test -Dtest=AppointmentServiceTest
```
All tests are integrated into GitHub Actions, executing on every push to guarantee zero regression.
