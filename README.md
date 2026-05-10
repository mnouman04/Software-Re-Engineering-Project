# Hospital Management System - Software Re-Engineering Project

Solves the day-to-day pain of paper-based OPD record keeping. The legacy flow overloaded doctors, receptionists, and administrators with slow storage and retrieval, inconsistent data, and unclear prescriptions. This project replaces that workflow with role-based accounts, centralized patient records, and one-click access to visit history, all delivered as a Java web application with MySQL, Spring MVC, and Hibernate.

---

## Project goals

- Move OPD records from paper to structured, searchable data
- Reduce time waste in storage and retrieval
- Provide separate, secure accounts for doctors and staff
- Keep each patient record isolated and track visit history instantly
- Generate prescription PDFs to reduce manual writing

---

## Tech stack

- Java, Spring MVC, Hibernate
- MySQL
- Maven

---

## Core features

- Role-based access for doctors, receptionists, and administrators
- Patient registration and visit history tracking
- OPD queue management and workflow handling
- Employee management (add, edit, remove)
- Prescription PDF generation

---

## Commands (SonarQube, DB schema, ETL)

### Start SonarQube

Option A: Docker (recommended)

```bash
docker run --name sonarqube -p 9000:9000 sonarqube:lts-community
```

Option B: Local SonarQube zip install (Windows)

```bat
cd C:\path\to\sonarqube
.\bin\windows-x86-64\StartSonar.bat
```

### Run SonarScanner

This project uses [RE_Final/src/main/java/org/example/sonar-project.properties](RE_Final/src/main/java/org/example/sonar-project.properties). Use a SonarQube token and run the scanner from the repository root.

```bat
set SONAR_TOKEN=your_token_here
sonar-scanner -Dproject.settings=RE_Final/src/main/java/org/example/sonar-project.properties -Dsonar.login=%SONAR_TOKEN%
```

### Load the hospital schema

The schema and table dumps are stored in [databaseFiles and demoLoginCredentials/hospitaldb](databaseFiles%20and%20demoLoginCredentials/hospitaldb).

```bat
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hospital;"
mysql -u root -p hospital < "databaseFiles and demoLoginCredentials/hospitaldb/hospital_login.sql"
mysql -u root -p hospital < "databaseFiles and demoLoginCredentials/hospitaldb/hospital_employee.sql"
mysql -u root -p hospital < "databaseFiles and demoLoginCredentials/hospitaldb/hospital_patient.sql"
mysql -u root -p hospital < "databaseFiles and demoLoginCredentials/hospitaldb/hospital_idgenerate.sql"
mysql -u root -p hospital < "databaseFiles and demoLoginCredentials/hospitaldb/hospital_opd.sql"
mysql -u root -p hospital < "databaseFiles and demoLoginCredentials/hospitaldb/hospital_opddetails.sql"
mysql -u root -p hospital < "databaseFiles and demoLoginCredentials/hospitaldb/hospital_routines.sql"
```

Manual seed steps (admin login, idgenerate row) are listed in [databaseFiles and demoLoginCredentials/tableQueries_SetupGuide.txt](databaseFiles%20and%20demoLoginCredentials/tableQueries_SetupGuide.txt).

### Execute the ETL migration script

The ETL uses [ETLPipeline/etl_migration.py](ETLPipeline/etl_migration.py) and expects a legacy appointments CSV in the same folder.

```bat
python ETLPipeline\etl_migration.py
```

---

## Documentation map (part-wise)

### Part A: Code smell analysis and audits

- Full analysis report: [CODE_SMELL_ANALYSIS.md](CODE_SMELL_ANALYSIS.md)
- Quick reference summary: [CODE_SMELL_QUICK_REFERENCE.md](CODE_SMELL_QUICK_REFERENCE.md)
- Change preventors audit: [CHANGE_PREVENTORS_AUDIT.md](CHANGE_PREVENTORS_AUDIT.md)
- Couplers audit: [COUPLERS_AUDIT.md](COUPLERS_AUDIT.md)
- Dispensables audit: [DISPENSABLES_AUDIT.md](DISPENSABLES_AUDIT.md)
- OO abusers audit: [OO_ABUSERS_AUDIT.md](OO_ABUSERS_AUDIT.md)

### Part B3: Smell interaction and prioritization

- Primary write-up: [B3_SMELL_INTERACTION_PRIORITISATION.md](B3_SMELL_INTERACTION_PRIORITISATION.md)
- Quick reference: [B3_QUICK_REFERENCE.md](B3_QUICK_REFERENCE.md)
- Before/after code comparison: [CODE_COMPARISON_BEFORE_AFTER.md](CODE_COMPARISON_BEFORE_AFTER.md)
- Impact summary and metrics: [REFACTORING_IMPACT_SUMMARY.md](REFACTORING_IMPACT_SUMMARY.md)
- Refactored examples: [REFACTORED_EXAMPLES](REFACTORED_EXAMPLES)
- Navigation guide: [README_B3_DOCUMENTS.md](README_B3_DOCUMENTS.md)

### Part C: Dependency mapping

- Coupling metrics and analysis: [PART_C_DEPENDENCY_MAPPING.md](PART_C_DEPENDENCY_MAPPING.md)

### Sections 8-10: Refactoring demonstration

- Start here: [START_HERE_SECTIONS_8_9_10.md](START_HERE_SECTIONS_8_9_10.md)
- Quick copy-paste content: [SECTION_8_9_10_QUICK_COPY_PASTE.md](SECTION_8_9_10_QUICK_COPY_PASTE.md)
- Comprehensive walkthrough: [SECTION_8_9_10_REFACTORING_DEMO.md](SECTION_8_9_10_REFACTORING_DEMO.md)
- Functional equivalence tests: [TEST_VERIFICATION_FUNCTIONAL_EQUIVALENCE.md](TEST_VERIFICATION_FUNCTIONAL_EQUIVALENCE.md)
- Navigation guide: [README_SECTIONS_8_9_10.md](README_SECTIONS_8_9_10.md)
- Refactored controller example: [REFACTORED_EXAMPLES/AddOpdController_REFACTORED.java](REFACTORED_EXAMPLES/AddOpdController_REFACTORED.java)
- Package summary: [PACKAGE_SUMMARY_SECTIONS_8_9_10.md](PACKAGE_SUMMARY_SECTIONS_8_9_10.md)

---

## Database and demo assets

- Setup guide: [databaseFiles and demoLoginCredentials/tableQueries_SetupGuide.txt](databaseFiles%20and%20demoLoginCredentials/tableQueries_SetupGuide.txt)
- Demo logins: [databaseFiles and demoLoginCredentials/loginPasswordsForDemo.txt](databaseFiles%20and%20demoLoginCredentials/loginPasswordsForDemo.txt)
- SQL dumps: [databaseFiles and demoLoginCredentials/hospitaldb](databaseFiles%20and%20demoLoginCredentials/hospitaldb)

---

## Additional resources

- Javadoc site: [doc/index.html](doc/index.html)
- ETL migration helper: [ETLPipeline/etl_migration.py](ETLPipeline/etl_migration.py)

---

## Project structure (high level)

```
HospitalManagement/
	src/                          Application source
	RE_Final/                     Packaged build output and related files
	databaseFiles and demoLoginCredentials/   MySQL setup and demo access
	doc/                          Generated Javadoc site
	REFACTORED_EXAMPLES/          Refactored code samples
	ETLPipeline/                  Data migration helper
```