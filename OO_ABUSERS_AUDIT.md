# OO Abusers Audit for Hospital Management System

## 1. Switch Statements / Complex If-Else Chains

### AddOpdController.add()
- **File**: `src/main/java/com/project/controller/opd/AddOpdController.java`
- **Lines**: 37-67
- **Evidence**:
  - `if(! doctorid.equals(null))` at line 37
  - `int b=dao.add(q1);` at line 41
  - `if(b==1)` at line 44
  - `else if(b==2)` at line 51
  - `else if(b==3)` at line 59
- **OO Principle Violated**: Open-Closed Principle, Single Responsibility Principle
- **Why**: The controller branches on integer return codes from DAO behavior instead of deferring behavior to polymorphic result objects or exceptions. Adding a new outcome requires editing this controller method.
- **Treatment**: Replace with Polymorphism or Result Object pattern
  - Move outcome handling into a service or domain object
  - Return `OpdResult` or throw typed exceptions like `AlreadyInQueueException` / `DoctorUnavailableException`
  - Controller should only render view based on a sealed result type, not integer codes

## 2. Temporary Fields

### LoginController.dao2
- **File**: `src/main/java/com/project/controller/LoginController.java`
- **Lines**: 26-27, 50-91
- **Evidence**:
  - `@Autowired UsersInSystemDao dao2;` at lines 26-27
  - `dao2.getUsersInSystem()` only used in `validate(...)` between lines 68-69 and line 76
- **OO Principle Violated**: Single Responsibility Principle / Law of Demeter
- **Why**: `LoginController` holds a DAO field that is only relevant to dashboard preparation during login validation. This suggests the dependency belongs in a dedicated service or facade instead of as a long-lived controller field.
- **Treatment**: Replace field with a dedicated `LoginDashboardService` or `UserSessionService`
  - Inject a service that encapsulates both prescription count and user count
  - Remove `dao2` from controller fields
  - Keep controller focused on request handling only

### AddOpdController.dao1
- **File**: `src/main/java/com/project/controller/opd/AddOpdController.java`
- **Lines**: 21-22, 44-49
- **Evidence**:
  - `@Autowired PatientPrescriptionDao dao1;` at lines 21-22
  - `dao1.prescriptionPrintCount()` only used inside the success branch at line 48
- **OO Principle Violated**: Single Responsibility Principle
- **Why**: `AddOpdController` depends on a receptionist-specific DAO in one outcome branch instead of delegating result details to a service.
- **Treatment**: Move dashboard data enrichment into a service or view-model builder
  - `OpdService.addOpd(...)` can return `AddOpdOutcome`
  - The controller need not store `dao1` directly

## 3. Alternative Classes with Different Interfaces

### SearchPatientDao vs SearchEmployeeDao
- **Files**:
  - `src/main/java/com/project/dao/receptionist/SearchPatientDao.java`
  - `src/main/java/com/project/dao/administrator/SearchEmployeeDao.java`
- **Evidence**:
  - `SearchPatientDao` has methods: `searchName(...)` at line 23, `searchId(...)` at line 47, `searchMobileNo(...)` at line 69, `searchAdharNo(...)` at line 91, `searchDoctorAssigned(...)` at line 113
  - `SearchEmployeeDao` has methods: `searchName(...)` at line 26, `searchId(...)` at line 52, `searchMobileNo(...)` at line 75, `searchAadharNo(...)` at line 98
- **OO Principle Violated**: Interface Segregation Principle / DRY
- **Why**: Two DAO classes expose strongly similar search operations with only entity type and parameter type changed. Their interface divergence forces callers to use separate APIs for conceptually identical tasks.
- **Treatment**: Replace with common DAO/service abstraction
  - Introduce `PersonSearchDao<T>` or `SearchDao<T>` with methods like `findByName(...)`, `findById(...)`, `findByMobile(...)`, `findByAadhar(...)`
  - Keep patient/employee-specific query details in separate strategy or repository implementations

### EditPatientDao vs EditEmployeeDao
- **Files**:
  - `src/main/java/com/project/dao/receptionist/EditPatientDao.java`
  - `src/main/java/com/project/dao/administrator/EditEmployeeDao.java`
- **Evidence**:
  - `EditPatientDao.edit(...)` signature at line 24
  - `EditEmployeeDao.edit(...)` signature at line 28
  - Both build update HQL queries with nearly identical parameter mapping patterns at lines 30-44 (Patient) and 34-47 (Employee)
- **OO Principle Violated**: Liskov Substitution Principle / DRY
- **Why**: Two classes perform the same edit/update work with differing interfaces and nearly duplicated query plumbing. This inconsistent interface increases maintenance cost.
- **Treatment**: Replace with a shared edit service or DAO base class
  - Use `AbstractEditDao<T>` or a common `updateEntity(...)` strategy
  - Standardize the edit interface with a parameter object or `UpdateRequest`

### AddPatientDao vs AddEmployeeDao
- **Files**:
  - `src/main/java/com/project/dao/receptionist/AddPatientDao.java`
  - `src/main/java/com/project/dao/administrator/AddEmployeeDao.java`
- **Evidence**:
  - Both expose `add(...)` methods with one entity parameter and use Hibernate session save plus ID generation logic
  - Patient uses `boolean add(Patient p1)` and Employee uses `boolean add(Employee e)`
- **OO Principle Violated**: DRY / Open-Closed Principle
- **Why**: Two add classes duplicate transaction and ID-generation behavior via separate interfaces instead of a common repository pattern.
- **Treatment**: Replace with a shared `AbstractAddDao<T>` or `EntityCreationService<T>`

## 4. Refused Bequest

### Explicit refused bequest check
- **Result**: No explicit inheritance-based refused bequest found in analyzed source
- **Evidence**: Project search of `extends` in `src/main/java` returned no explicit subclass declarations
- **Interpretation**: There is currently no direct subclass hierarchy that violates LSP by ignoring inherited behavior.
- **Related design risk**: `Patient` and `Employee` share duplicated fields and constructor patterns, so if a future design introduces inheritance, it will likely produce refused bequest.
- **Treatment**: Prefer delegation or composition over inheritance for shared person data
  - Extract shared domain concepts into `PersonInfo`, `Name`, `Address`, `ContactDetails`
  - Avoid making `Patient extends Employee` or vice versa
  - Use `PatientFactory` / `EmployeeFactory` or shared service components instead

## 5. Recommended Immediate Refactorings
- Replace integer-coded branching in `AddOpdController` with a service result or typed exceptions
- Consolidate `SearchPatientDao` and `SearchEmployeeDao` into a common search abstraction
- Refactor `LoginController` to delegate dashboard data fetching to a dedicated service instead of holding `dao2`
- Introduce shared add/edit DAOs or services for patient/employee persistence operations
- Keep any future shared behavior as composition/delegation rather than inheritance to avoid LSP/Refused Bequest
