# 🌿 GitHub Workflow, Version Control & CI/CD Strategy

**Module:** CIS6003 Advanced Programming  
**Document:** Git Version Control & Deployment Engineering (LO III: 20 Marks)

---

## 1. Version Control Strategy & Git Flow Model

The development of the **Sunrise Dental Clinic Management System** utilized an industry-standard **Git Flow** branching methodology to maintain code quality, facilitate collaborative development, and ensure production stability.

```
  main      ───●───────────────────────────● (v1.0.0 Release)
                \                         /
  develop   ─────●─────────●─────────────●
                  \       / \           /
  feature/* ───────●─────●   ──●───────●
             (Auth Feature)   (Billing Feature)
```

---

## 2. Branching Structure

| Branch Name | Purpose & Lifecycle | Protection Level |
|---|---|---|
| `main` | Production-ready stable code. Only merged via tested Pull Requests. Every merge tagged with a semantic version (e.g. `v1.0.0`). | **Protected**: Requires passing CI/CD tests. |
| `develop` | Integration branch where feature branches are aggregated and tested. | **Protected**: Automated CI checks required. |
| `feature/authentication` | Implemented `UserDAO`, `AuthService`, BCrypt hashing, and `LoginServlet`. | Short-lived branch. |
| `feature/appointment-management` | Implemented `PatientDAO`, `AppointmentDAO`, double-booking triggers, and booking UI. | Short-lived branch. |
| `feature/billing-and-invoicing` | Implemented MySQL Stored Procedure, `InvoiceDAO`, `BillingService`, and printable receipts. | Short-lived branch. |
| `feature/executive-reports` | Implemented MySQL Views, `ReportDAO`, `ReportService`, and analytics dashboard. | Short-lived branch. |
| `feature/automated-testing` | Implemented JUnit 5 unit tests, Mockito service tests, and Selenium UI tests. | Short-lived branch. |
| `feature/ci-cd-pipeline` | Configured GitHub Actions `.github/workflows/ci.yml` and deployment descriptors. | Short-lived branch. |

---

## 3. Commit Message Conventions (Conventional Commits)

All commits in the repository follow the structured **Conventional Commits** standard:

- `feat(auth): implement BCrypt password hashing and LoginServlet`
- `feat(appointment): add double-booking prevention trigger and booking service`
- `feat(billing): integrate Stored Procedure GenerateBillForAppointment and print CSS`
- `feat(reports): add MySQL views for daily workload and monthly revenue`
- `test(unit): add JUnit 5 test suite for ValidationUtil and PasswordUtil`
- `test(mock): add Mockito tests for AuthService and BillingService`
- `ci(github-actions): create automated maven build, test and war packaging workflow`
- `docs(academic): create complete 4000-word academic report and UML diagrams`

---

## 4. Pull Request & Code Review Workflow

```
1. Developer creates feature branch from 'develop'
   $ git checkout -b feature/appointment-management develop

2. Developer makes atomic commits & pushes to remote
   $ git push -u origin feature/appointment-management

3. Pull Request opened against 'develop' on GitHub
   - Automated CI Pipeline triggers immediately
   - Maven executes `mvn clean test` across all test suites
   - Code review checklist verified

4. PR merged into 'develop' upon passing all automated tests

5. Release merged into 'main' and tagged with version
   $ git tag -a v1.0.0 -m "Release v1.0.0: Complete Sunrise Dental Clinic System"
   $ git push origin v1.0.0
```

---

## 5. Automated CI/CD Pipeline (`.github/workflows/ci.yml`)

The pipeline comprises two automated stages:

### Stage 1: Build, Test & Package
- **Environment**: Ubuntu 22.04 LTS runner with JDK 17 (Eclipse Temurin).
- **Dependency Caching**: Maven `.m2` repository cached between runs to reduce build times.
- **Unit & Mock Testing**: Automated invocation of `mvn clean test`. If any test fails, the build is blocked.
- **Artifact Packaging**: Executes `mvn package -DskipTests` to produce `sunrise-dental-clinic.war`.
- **Artifact Archiving**: Uses `actions/upload-artifact@v4` to store the `.war` binary for 14 days.

### Stage 2: Automated Deployment to Tomcat 10.1 (Continuous Delivery)
- **Condition**: Executes on push to `main`.
- **Mechanism**: Connects to the Apache Tomcat Manager API using GitHub Secrets (`TOMCAT_HOST`, `TOMCAT_USERNAME`, `TOMCAT_PASSWORD`) to upload and deploy the WAR file live.

---

## 6. How to Clone, Build and Run Locally

```bash
# 1. Clone repository
git clone https://github.com/MalithShehan/BSCSE_CIS6003_20374225.git
cd BSCSE_CIS6003_20374225

# 2. Import Database
Get-Content database/sunrise_dental_clinic.sql | mysql -u root -p

# 3. Execute Tests
mvn clean test

# 4. Package WAR file
mvn package

# 5. Deploy to Apache Tomcat webapps directory
cp target/sunrise-dental-clinic.war /path/to/tomcat/webapps/
```
