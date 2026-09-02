# 📐 PlantUML Design Models — Sunrise Dental Clinic

**Module:** CIS6003 Advanced Programming  
**Document:** Complete UML Specifications (Targeting Excellent 70–100% Grade Band)

---

## 1. Use Case Diagram

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle
skinparam handwritten false
skinparam monochrome false
skinparam shadowing false
skinparam defaultFontName Arial

actor "System Staff" as Staff <<Abstract>>
actor "Receptionist" as Receptionist
actor "Dentist" as Dentist
actor "Clinic Administrator" as Admin

Staff <|-- Receptionist
Staff <|-- Dentist
Staff <|-- Admin

rectangle "Sunrise Dental Clinic System" {
    usecase "Authenticate (Login)" as UC_Login
    usecase "View Session / Profile" as UC_Session
    usecase "Logout" as UC_Logout
    
    usecase "Register New Patient" as UC_RegPatient
    usecase "Book New Appointment" as UC_BookApp
    usecase "Check Schedule Availability" as UC_CheckSlot
    usecase "Dispatch Simulated Alert (SMS/Email)" as UC_Notify
    
    usecase "Search Appointment by Ref Number" as UC_SearchApp
    usecase "Cancel Scheduled Appointment" as UC_CancelApp
    usecase "View Clinical Schedule" as UC_ViewSchedule
    
    usecase "Calculate Bill Breakdown" as UC_CalcBill
    usecase "Apply Promotional Discount" as UC_ApplyDiscount
    usecase "Generate Official Invoice" as UC_GenInvoice
    usecase "Print Patient Receipt" as UC_PrintReceipt
    
    usecase "View Decision-Support Reports" as UC_Reports
    usecase "Export Daily Appointment Report" as UC_DailyRep
    usecase "Audit Monthly Revenue Report" as UC_MonthlyRep
    usecase "Evaluate Dentist Performance" as UC_DentistRep
}

' Staff Core Interactions
Staff --> UC_Login
Staff --> UC_Session
Staff --> UC_Logout

' Receptionist Interactions
Receptionist --> UC_BookApp
Receptionist --> UC_SearchApp
Receptionist --> UC_CancelApp
Receptionist --> UC_GenInvoice
Receptionist --> UC_PrintReceipt

' Dentist Interactions
Dentist --> UC_ViewSchedule
Dentist --> UC_SearchApp

' Admin Interactions
Admin --> UC_Reports
Admin --> UC_BookApp
Admin --> UC_GenInvoice

' Relationships (<<include>> and <<extend>>)
UC_BookApp ..> UC_CheckSlot : <<include>>
UC_BookApp ..> UC_RegPatient : <<include>>
UC_BookApp ..> UC_Notify : <<include>>

UC_GenInvoice ..> UC_CalcBill : <<include>>
UC_ApplyDiscount ..> UC_GenInvoice : <<extend>>
UC_PrintReceipt ..> UC_GenInvoice : <<extend>>

UC_Reports ..> UC_DailyRep : <<include>>
UC_Reports ..> UC_MonthlyRep : <<include>>
UC_Reports ..> UC_DentistRep : <<include>>

@enduml
```

---

## 2. Complete Class Diagram

```plantuml
@startuml
skinparam classAttributeIconSize 0
skinparam defaultFontName Arial
skinparam shadowing false

package "Model Layer" {
    class User {
        - userId : int
        - username : String
        - passwordHash : String
        - fullName : String
        - role : String
        - active : boolean
        - createdAt : Timestamp
        - updatedAt : Timestamp
        + getUserId() : int
        + getUsername() : String
        + getRole() : String
        + isActive() : boolean
    }

    class Patient {
        - patientId : int
        - patientName : String
        - address : String
        - contactNumber : String
        - email : String
        - createdAt : Timestamp
        + getPatientId() : int
        + getPatientName() : String
        + getContactNumber() : String
    }

    class Dentist {
        - dentistId : int
        - userId : Integer
        - dentistName : String
        - specialization : String
        - consultationFee : BigDecimal
        - contactNumber : String
        - active : boolean
        + getDentistId() : int
        + getConsultationFee() : BigDecimal
        + getDentistName() : String
    }

    class Treatment {
        - treatmentId : int
        - treatmentName : String
        - description : String
        - cost : BigDecimal
        - active : boolean
        + getTreatmentId() : int
        + getCost() : BigDecimal
        + getTreatmentName() : String
    }

    class Appointment {
        - appointmentId : int
        - appointmentNumber : String
        - patientId : int
        - dentistId : int
        - treatmentId : int
        - appointmentDate : Date
        - appointmentTime : Time
        - status : String
        - notes : String
        - createdAt : Timestamp
        + getAppointmentNumber() : String
        + getStatus() : String
        + setStatus(status : String) : void
    }

    class Invoice {
        - invoiceId : int
        - invoiceNumber : String
        - appointmentId : int
        - consultationFee : BigDecimal
        - treatmentCost : BigDecimal
        - discountPercentage : BigDecimal
        - discountAmount : BigDecimal
        - totalAmount : BigDecimal
        - paymentStatus : String
        - paymentMethod : String
        - createdAt : Timestamp
        + getInvoiceNumber() : String
        + getTotalAmount() : BigDecimal
    }

    class ApiResponse<T> {
        - success : boolean
        - message : String
        - data : T
        + ok(msg : String, data : T) : ApiResponse<T>
        + error(msg : String) : ApiResponse<T>
    }
}

package "Data Access Object (DAO) Layer" {
    class UserDAO {
        + findByUsername(username : String) : User
        + findById(userId : int) : User
        + createUser(user : User) : int
    }

    class PatientDAO {
        + findById(id : int) : Patient
        + findByContactNumber(contact : String) : Patient
        + createPatient(patient : Patient) : int
    }

    class DentistDAO {
        + findAllActive() : List<Dentist>
        + findById(id : int) : Dentist
    }

    class TreatmentDAO {
        + findAllActive() : List<Treatment>
        + findById(id : int) : Treatment
    }

    class AppointmentDAO {
        + createAppointment(app : Appointment) : int
        + findByAppointmentNumber(num : String) : Appointment
        + isDoubleBooked(dentistId : int, d : Date, t : Time, exId : Integer) : boolean
        + getNextSequenceNumber(year : int) : int
        + updateStatus(id : int, status : String) : boolean
    }

    class InvoiceDAO {
        + generateBill(appId : int, discount : BigDecimal, method : String) : Invoice
        + findByAppointmentNumber(appNum : String) : Invoice
        + findByAppointmentId(appId : int) : Invoice
    }

    class ReportDAO {
        + getDailyAppointmentReport() : List<ReportItem>
        + getMonthlyRevenueReport() : List<ReportItem>
        + getDentistPerformanceReport() : List<ReportItem>
    }
}

package "Service Layer" {
    class AuthService {
        - userDAO : UserDAO
        + authenticate(username : String, pass : String) : User
        + registerUser(u : String, p : String, n : String, r : String) : User
    }

    class AppointmentService {
        - appointmentDAO : AppointmentDAO
        - patientDAO : PatientDAO
        - dentistDAO : DentistDAO
        - treatmentDAO : TreatmentDAO
        - emailNotifier : NotificationService
        - smsNotifier : NotificationService
        + bookAppointment(name : String, addr : String, phone : String, email : String, dId : int, tId : int, date : Date, time : Time, notes : String) : Appointment
        + getAppointmentByNumber(num : String) : Appointment
        + updateAppointmentStatus(id : int, status : String) : boolean
    }

    class BillingService {
        - invoiceDAO : InvoiceDAO
        - appointmentDAO : AppointmentDAO
        - smsNotifier : NotificationService
        + generateBill(appId : int, discount : BigDecimal, method : String) : Invoice
        + getInvoiceByAppointmentNumber(num : String) : Invoice
    }

    interface NotificationService {
        + sendAppointmentNotification(app : Appointment) : boolean
        + sendBillingNotification(appNum : String, phone : String, amount : double) : boolean
    }

    class EmailNotificationService implements NotificationService
    class SmsNotificationService implements NotificationService

    class NotificationFactory {
        + {static} getNotificationService(channel : Channel) : NotificationService
    }
}

package "Configuration & Utility Layer" {
    class DatabaseConnection <<Singleton>> {
        - {static} instance : DatabaseConnection
        - driver : String
        - url : String
        - username : String
        - password : String
        - DatabaseConnection()
        + {static} getInstance() : DatabaseConnection
        + getConnection() : Connection
    }

    class PasswordUtil {
        + {static} hashPassword(plain : String) : String
        + {static} verifyPassword(candidate : String, hash : String) : boolean
    }

    class ValidationUtil {
        + {static} isValidPhoneNumber(phone : String) : boolean
        + {static} isWithinClinicHours(time : Time) : boolean
        + {static} isFutureOrTodayDate(date : Date) : boolean
    }

    class AppointmentNumberGenerator {
        + {static} format(year : int, seq : int) : String
    }
}

' Multiplicity & Structural Relationships
Patient "1" *-- "0..*" Appointment : books >
Dentist "1" o-- "0..*" Appointment : attends >
Treatment "1" o-- "0..*" Appointment : prescribes >
Appointment "1" *-- "0..1" Invoice : settles into >
User "0..1" -- "1" Dentist : associates >

AppointmentService o-- AppointmentDAO
AppointmentService o-- PatientDAO
AppointmentService o-- DentistDAO
AppointmentService o-- TreatmentDAO
AppointmentService o-- NotificationService

BillingService o-- InvoiceDAO
BillingService o-- AppointmentDAO

AuthService o-- UserDAO

@enduml
```

---

## 3. Sequence Diagram 1: Staff Authentication (Login)

```plantuml
@startuml
autonumber
skinparam defaultFontName Arial
skinparam shadowing false

actor "Staff User" as User
boundary "login.html / auth.js" as UI
control "LoginServlet" as Controller
participant "AuthService" as Service
participant "UserDAO" as DAO
database "MySQL Database" as DB
participant "HttpSession" as Session

User -> UI : Enter Username & Password and Click 'Sign In'
UI -> Controller : POST /api/login { username, password }
activate Controller

Controller -> Service : authenticate(username, password)
activate Service

Service -> DAO : findByUsername(username)
activate DAO
DAO -> DB : SELECT * FROM users WHERE username = ?
DB --> DAO : ResultSet (user_id, password_hash, role, is_active)
DAO --> Service : User entity
deactivate DAO

alt User Not Found OR Inactive
    Service --> Controller : throw SecurityException("Invalid credentials or inactive")
    Controller --> UI : HTTP 401 Unauthorized { success: false, message }
    UI --> User : Display Error Alert
else User Found & Active
    Service -> Service : PasswordUtil.verifyPassword(candidate, storedHash)
    alt Password Mismatch
        Service --> Controller : throw SecurityException("Invalid credentials")
        Controller --> UI : HTTP 401 Unauthorized { success: false }
    else Password Match
        Service --> Controller : return User (Authenticated)
        deactivate Service
        
        Controller -> Session : create session & setAttribute("user", user)
        Controller --> UI : HTTP 200 OK { success: true, data: sanitizedUser }
        deactivate Controller
        
        UI --> User : Redirect to dashboard.html
    end
end
@enduml
```

---

## 4. Sequence Diagram 2: Register New Patient Appointment

```plantuml
@startuml
autonumber
skinparam defaultFontName Arial
skinparam shadowing false

actor "Receptionist" as Rec
boundary "register-appointment.html / appointment.js" as UI
control "AppointmentServlet" as Servlet
participant "AppointmentService" as Service
participant "DentistDAO" as D_DAO
participant "TreatmentDAO" as T_DAO
participant "PatientDAO" as P_DAO
participant "AppointmentDAO" as A_DAO
database "MySQL (sunrise_dental_db)" as DB
participant "NotificationService" as Notify

Rec -> UI : Input Patient & Booking info, Click 'Confirm & Schedule'
UI -> UI : Validation.js (Phone format, Date != Past, Time within 08:00-17:00)

UI -> Servlet : POST /api/appointments { patientName, phone, dentistId, treatmentId, date, time }
activate Servlet

Servlet -> Service : bookAppointment(...)
activate Service

Service -> D_DAO : findById(dentistId)
D_DAO --> Service : Dentist (active, fee: 2500.00)

Service -> T_DAO : findById(treatmentId)
T_DAO --> Service : Treatment (active, cost: 4500.00)

Service -> A_DAO : isDoubleBooked(dentistId, date, time)
A_DAO -> DB : SELECT COUNT(*) FROM appointments WHERE dentist_id=? AND date=? AND time=? AND status!='CANCELLED'
DB --> A_DAO : count

alt Dentist Double Booked (count > 0)
    A_DAO --> Service : true
    Service --> Servlet : throw IllegalStateException("Dentist already booked at this time slot")
    Servlet --> UI : HTTP 409 Conflict { success: false, message }
    UI --> Rec : Show Conflict Error Alert
else Available Slot
    A_DAO --> Service : false
    
    Service -> P_DAO : createPatient(patient)
    P_DAO -> DB : INSERT INTO patients (...) VALUES (...)
    DB --> P_DAO : generated patient_id
    P_DAO --> Service : patient_id
    
    Service -> A_DAO : getNextSequenceNumber(2026)
    A_DAO --> Service : seq (e.g. 1)
    Service -> Service : AppointmentNumberGenerator.format(2026, 1) -> "SDC-2026-0001"
    
    Service -> A_DAO : createAppointment(appointment)
    A_DAO -> DB : INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, date, time, status)
    note right of DB: Trigger log_appointment_insert automatically writes to appointment_status_log
    DB --> A_DAO : generated appointment_id
    A_DAO --> Service : appointment_id
    
    Service -> Notify : sendAppointmentNotification(appointment)
    activate Notify
    Notify --> Service : notification logged/sent
    deactivate Notify
    
    Service --> Servlet : return Appointment ("SDC-2026-0001")
    deactivate Service
    
    Servlet --> UI : HTTP 201 Created { success: true, data: appointment }
    deactivate Servlet
    
    UI --> Rec : Display Success Confirmation Banner with SDC-2026-0001
end
@enduml
```

---

## 5. Sequence Diagram 3: Calculate & Generate Invoice Bill

```plantuml
@startuml
autonumber
skinparam defaultFontName Arial
skinparam shadowing false

actor "Receptionist" as Rec
boundary "bill.html / bill.js" as UI
control "BillServlet" as Servlet
participant "BillingService" as Service
participant "AppointmentDAO" as A_DAO
participant "InvoiceDAO" as I_DAO
database "MySQL Stored Procedure" as SP
database "MySQL Database" as DB
participant "NotificationService" as Notify

Rec -> UI : Enter SDC-2026-0001, Discount 10%, Click 'Issue Official Invoice'
UI -> Servlet : POST /api/bill { appointmentId: 1, discountPercentage: 10.0, paymentMethod: "CASH" }
activate Servlet

Servlet -> Service : generateBill(1, 10.0, "CASH")
activate Service

Service -> A_DAO : findById(1)
A_DAO --> Service : Appointment (Status: "SCHEDULED", Fee: 2500, Treatment: 4500)

Service -> I_DAO : findByAppointmentId(1)
I_DAO --> Service : null (Not yet billed)

Service -> I_DAO : generateBill(1, 10.0, "CASH")
activate I_DAO

I_DAO -> SP : CallableStatement: CALL GenerateBillForAppointment(1, 10.0, 'CASH', ?, ?, ?)
activate SP

SP -> DB : Validate Appointment Not Cancelled & Not Already Invoiced
SP -> DB : Compute Subtotal (2500 + 4500 = 7000.00)
SP -> DB : Compute Discount (7000 * 0.10 = 700.00), Net Total = 6300.00
SP -> DB : INSERT INTO invoices (invoice_number, total_amount, payment_status, ...) VALUES ('INV-2026-0001', 6300.00, 'PAID', ...)
SP -> DB : UPDATE appointments SET status = 'COMPLETED' WHERE appointment_id = 1
note right of DB: Trigger log_appointment_status_change logs COMPLETED status to appointment_status_log
SP --> I_DAO : OUT params (p_invoice_id: 1, p_invoice_number: 'INV-2026-0001', p_total_amount: 6300.00)
deactivate SP

I_DAO -> DB : SELECT * FROM invoices WHERE invoice_id = 1
DB --> I_DAO : Invoice Record
I_DAO --> Service : Invoice entity
deactivate I_DAO

Service -> Notify : sendBillingNotification("SDC-2026-0001", "0771234567", 6300.00)
Service --> Servlet : return Invoice
deactivate Service

Servlet --> UI : HTTP 201 Created { success: true, data: invoice }
deactivate Servlet

UI -> UI : Render Printable Invoice Template & Receipt Box
UI --> Rec : Display Printable Invoice & Allow 1-Click Print
@enduml
```

---

## 6. Distributed 3-Tier Architecture Diagram

```plantuml
@startuml
skinparam componentStyle uml2
skinparam defaultFontName Arial
skinparam shadowing false

package "Tier 1: Presentation Tier (Client Web Browser)" {
    [HTML5 Pages\n(login, dashboard, register, search, bill, reports)] as UI_HTML
    [CSS3 Stylesheet\n(style.css & @media print)] as UI_CSS
    [JavaScript ES6 Modules\n(api.js, auth.js, validation.js, appointment.js, bill.js, reports.js)] as UI_JS
    
    UI_HTML -down-> UI_CSS : styled by
    UI_HTML -down-> UI_JS : executes
}

cloud "HTTP / REST (JSON Payloads over TLS)" as Network

UI_JS -down-> Network : Fetch API requests (credentials: include)

package "Tier 2: Business Logic Tier (Apache Tomcat 10.1 Web Container)" {
    [AuthFilter\n(Session Security & CORS)] as Filter
    
    package "Jakarta Servlets (Controllers)" {
        [LoginServlet / LogoutServlet / SessionServlet] as AuthServlets
        [AppointmentServlet] as AppServlet
        [BillServlet] as BillServlet
        [ReportServlet] as RepServlet
        [DentistServlet / TreatmentServlet] as MasterServlets
    }
    
    package "Service Layer (Business Rules & Patterns)" {
        [AuthService] as S_Auth
        [AppointmentService] as S_App
        [BillingService] as S_Bill
        [ReportService] as S_Rep
        [NotificationFactory & NotificationService] as S_Notify
    }
    
    package "Data Access Layer (JDBC DAOs)" {
        [UserDAO] as DAO_User
        [PatientDAO] as DAO_Pat
        [DentistDAO & TreatmentDAO] as DAO_Master
        [AppointmentDAO] as DAO_App
        [InvoiceDAO] as DAO_Inv
        [ReportDAO] as DAO_Rep
        [DatabaseConnection (Singleton)] as DB_Pool
    }
    
    Network -down-> Filter
    Filter -down-> AuthServlets
    Filter -down-> AppServlet
    Filter -down-> BillServlet
    Filter -down-> RepServlet
    Filter -down-> MasterServlets
    
    AuthServlets -down-> S_Auth
    AppServlet -down-> S_App
    BillServlet -down-> S_Bill
    RepServlet -down-> S_Rep
    
    S_App -right-> S_Notify
    S_Bill -right-> S_Notify
    
    S_Auth -down-> DAO_User
    S_App -down-> DAO_App
    S_App -down-> DAO_Pat
    S_App -down-> DAO_Master
    S_Bill -down-> DAO_Inv
    S_Bill -down-> DAO_App
    S_Rep -down-> DAO_Rep
    
    DAO_User ..> DB_Pool : gets connection
    DAO_App ..> DB_Pool : gets connection
    DAO_Inv ..> DB_Pool : gets connection
    DAO_Rep ..> DB_Pool : gets connection
}

database "Tier 3: Data Tier (MySQL 8.0 Server)" {
    frame "sunrise_dental_db" {
        [Tables: users, patients, dentists, treatments, appointments, invoices, status_log] as Tables
        [Stored Procedures: GenerateBillForAppointment] as Procedures
        [Functions: GetAppointmentTotal] as Functions
        [Triggers: prevent_double_booking, log_appointment_status_change] as Triggers
        [Views: Daily_Appointment_Report, Monthly_Revenue_Report, Dentist_Performance_Report] as Views
    }
}

DB_Pool -down-> Tables : JDBC PreparedStatements
DAO_Inv -down-> Procedures : JDBC CallableStatements
DAO_Rep -down-> Views : SQL Queries
Tables -down-> Triggers : Fire on Insert/Update
@enduml
```
