# Hospital Management System - Code Smell Quick Reference

## Executive Summary
- **Total Issues Found**: 25+ distinct code smells
- **Code Duplication**: 39.7% (430-450 lines of duplicate code)
- **Files Analyzed**: 103 Java files
- **Risk Level**: CRITICAL - Immediate refactoring required

---

## ADDITIONAL CODE SMELLS (Refactoring.Guru Patterns)

### 1. LONG METHOD - A method with excessive lines of code
**Issue**: Method contains too much logic, requires scrolling, tightly coupled and fragile  
**Treatment**: Extract Method

#### Example 1: AddPatientDao.add() - 39 lines
**File**: src/main/java/com/project/dao/receptionist/AddPatientDao.java (Lines 56-94)
```java
@Transactional
public boolean add(Patient p1) 
{
    infoLog.logActivities("in AddPatientDao-add: got= "+p1);
    Date date= new Date();
    p1.setRegistrationDate(date);
    
    Session session= sf.getCurrentSession();
    session.save(p1);
    
    Query q1=session.createQuery(" from IdGenerate");
    IdGenerate temp= (IdGenerate) q1.uniqueResult();
    int pid=temp.getPid();
    infoLog.logActivities("in AddPatientDao-add: pid= "+pid);
    
    pid++;
    q1=session.createQuery("update IdGenerate set pid= :i");
    q1.setParameter("i", pid);
    int res= q1.executeUpdate();
    infoLog.logActivities("incremented pid= "+pid+" update status="+res);
    
    return true;
}
```
**Problem**: Method does 3 things: sets date, saves patient, increments ID counter
**Solution**: Extract to `extractSetRegistrationDate()`, `extractIncrementPatientId()`

#### Example 2: AddOpdController.add() - 50 lines
**File**: src/main/java/com/project/controller/opd/AddOpdController.java (Lines 26-75)
```java
@RequestMapping(value = "/addOpd.html", method =RequestMethod.POST)
public ModelAndView add(@RequestParam("pid")String pid) {
    try {
        infoLog.logActivities("in AddOpdController-add: got= "+pid);
        String doctorid=dao.getDoctorId(pid);
        infoLog.logActivities("returned to AddOpdController-add: got= "+doctorid);
        
        if(! doctorid.equals(null)) {
            Opd q1= new Opd(pid, doctorid, Opd.PENDING);
            int b=dao.add(q1);
            infoLog.logActivities("returned to AddOpdController-add: got= "+b);
            
            if(b==1) { /* create success view */ }
            else if(b==2) { /* create error view */ }
            else if(b==3) { /* create error view */ }
            else { throw new Exception(); }
        }
    } catch(Exception e) { /* error handling */ }
}
```
**Problem**: Controller logic contains: validation, DAO call, error branching, view creation
**Solution**: Extract to `validateAndAddOpd()`, `createOpdResponseView()`

---

### 2. LARGE CLASS - A class trying to do too much
**Issue**: Violates Single Responsibility Principle, low cohesion, hard to maintain  
**Treatment**: Extract Class into focused responsibilities

#### Example 1: LoginController - 6 responsibilities
**File**: src/main/java/com/project/controller/LoginController.java (Lines 17-92)
```java
@Controller
public class LoginController {
    @Autowired LoginDao dao;                        // Responsibility 1: Login
    @Autowired PatientPrescriptionDao dao1;         // Responsibility 2: Prescriptions
    @Autowired UsersInSystemDao dao2;               // Responsibility 3: User tracking
    
    @RequestMapping(value="/login.html", method = RequestMethod.POST)
    public ModelAndView view() { }                  // Responsibility 4: Display form
    
    @RequestMapping(value="/dashboard.html", method = RequestMethod.POST)
    public ModelAndView validate(...) {             // Responsibility 5-6: Validate + Dashboard
        mv.addObject("prescriptionsCount", dao1.prescriptionPrintCount());
        mv.addObject("users_count", dao2.getUsersInSystem());
        return mv;
    }
}
```
**Responsibilities**: 1) Form display 2) Credential validation 3) Role resolution 4) Prescription counting 5) User tracking 6) Session management  
**Solution**: Extract to `LoginFormController`, `LoginAuthenticationService`, `DashboardFacade`

#### Example 2: EditPatientController - 5+ responsibilities
**File**: src/main/java/com/project/controller/receptionist/EditPatientController.java
```java
@Controller 
public class EditPatientController {
    @Autowired PatientDetailsDao dao1;              // Responsibility 1: Fetch details
    @Autowired SearchPatientDao dao3;               // Responsibility 2: Search
    @Autowired EditPatientDao dao2;                 // Responsibility 3: Edit
    @Autowired AddPatientDao dao4;                  // Responsibility 4: List doctors
    @Autowired PatientPrescriptionDao dao5;         // Responsibility 5: Count prescriptions
    
    @RequestMapping(value="/editPatientView.html", method=RequestMethod.POST)
    public ModelAndView edit(@RequestParam("pid")String pid) { }
    
    @RequestMapping(value="/editPatient.html", method = RequestMethod.POST)
    public ModelAndView edit(...17 parameters...) { }
}
```
**Responsibilities**: 1) View preparation 2) Validation 3) Patient editing 4) Doctor assignment 5) Prescription display  
**Solution**: Extract to `PatientEditFacade`, `PatientEditService`, `DoctorService`

#### Example 3: SearchPatientDao - 4 similar methods
**File**: src/main/java/com/project/dao/receptionist/SearchPatientDao.java
```java
@Component
public class SearchPatientDao {
    @Transactional
    public Patient searchName(String firstName, String lastName) { }
    
    @Transactional
    public Patient searchId(String pid) { }
    
    @Transactional
    public Patient searchMobileNo(Long mobileNo) { }
    
    @Transactional
    public Patient searchAdharNo(Long adharNo) { }
    
    @Transactional
    public String searchDoctorAssigned(String doctorId) { }
}
```
**Problem**: 5 different search responsibilities in single class  
**Solution**: Extract to `GenericSearch<T>` or split into `PatientSearchService`, `DoctorSearchService`

---

### 3. LONG PARAMETER LIST - Too many parameters in method signature
**Issue**: Hard to call correctly, reveals class pulling data from too many places, fragile to changes  
**Treatment**: Introduce Parameter Object (DTO)

#### Example 1: EditPatientController.edit() - 17 parameters
**File**: src/main/java/com/project/controller/receptionist/EditPatientController.java (Lines 62-76)
```java
@RequestMapping(value="/editPatient.html", method = RequestMethod.POST)
public ModelAndView edit(
    @RequestParam("pid")String pid,                    // 1
    @RequestParam("firstName")String firstName,        // 2
    @RequestParam("middleName")String middleName,      // 3
    @RequestParam("lastName")String lastName,          // 4
    @RequestParam("birthdate")String birthdate,        // 5
    @RequestParam("gender")String gender,              // 6
    @RequestParam("email")String email,                // 7
    @RequestParam("mobileNo")Long mobileNo,            // 8
    @RequestParam("adharNo")Long adharNo,              // 9
    @RequestParam("country")String country,            // 10
    @RequestParam("state")String state,                // 11
    @RequestParam("city")String city,                  // 12
    @RequestParam("residentialAddress")String residentialAddress,  // 13
    @RequestParam("permanentAddress")String permanentAddress,      // 14
    @RequestParam("bloodGroup")String bloodGroup,      // 15
    @RequestParam("chronicDiseases")String chronicDiseases,        // 16
    @RequestParam("medicineAllergy")String medicineAllergy,        // 17
    @RequestParam("doctorId")String doctorId)          // 18
```
**Problem**: 17 parameters = impossible to understand, test, or modify  
**Current Code**: Lines 78-79 create Name and Address objects
```java
Name n1=new Name(firstName,middleName,lastName);
Address a1= new Address(residentialAddress, permanentAddress);
```
**Solution**: Replace with `PatientEditDTO`
```java
@RequestMapping(value="/editPatient.html", method = RequestMethod.POST)
public ModelAndView edit(@RequestParam PatientEditDTO patientDTO) {
    patientEditService.updatePatient(patientDTO);
    return successView();
}
```

#### Example 2: EditPatientDao.edit() - 15 parameters
**File**: src/main/java/com/project/dao/receptionist/EditPatientDao.java (Lines 24-30)
```java
@Transactional
public int edit(
    String pid,                    // 1
    Name name,                     // 2
    String birthdate,              // 3
    String gender,                 // 4
    String emailId,                // 5
    Long mobileNo,                 // 6
    Long adharNo,                  // 7
    String country,                // 8
    String state,                  // 9
    String city,                   // 10
    Address address,               // 11
    String bloodGroup,             // 12
    String chronicDiseases,        // 13
    String medicineAllergy,        // 14
    String doctorId)               // 15
```
**Problem**: 15 parameters + hardcoded HQL query (Lines 32-39)
```java
Query q1=session.createQuery("update Patient set " +
    "name.firstName= :t1, name.middleName= :t2, name.lastName= :t3, " +
    "birthdate= :t4, emailId= :t5, mobileNo= :t6, " +
    "country= :t7, state= :t8, city=:t9, " +
    "address.residentialAddress= :t10, chronicDiseases= :t13, " +
    "medicineAllergy= :t14, doctorId= :t15 where pid= :id");
q1.setParameter("t1", name.getFirstName());
q1.setParameter("t2", name.getMiddleName());
// ... 13 more setParameter calls
```
**Solution**: Use Spring Data JPA + DTO
```java
public void updatePatient(PatientUpdateDTO dto) {
    patientRepository.save(PatientMapper.toDomain(dto));
}
```

#### Example 3: EditEmployeeController.edit() - 16 parameters
**File**: src/main/java/com/project/controller/administrator/EditEmployeeController.java (Lines 70-84)
```java
@RequestMapping(value="/editEmployee.html", method = RequestMethod.POST)
public ModelAndView edit(
    @RequestParam("eid")String eid,                    // 1
    @RequestParam("firstName")String firstName,        // 2
    @RequestParam("middleName")String middleName,      // 3
    @RequestParam("lastName")String lastName,          // 4
    @RequestParam("birthdate")String birthdate,        // 5
    // ... 11 more parameters through line 83
    @RequestParam("specialization")String specialization) // 16
```
**Solution**: Use `EmployeeEditDTO` instead

#### Example 4: AddEmployeeController.add() - 14+ parameters  
**File**: src/main/java/com/project/controller/administrator/AddEmployeeController.java (Lines 46-55)
```java
@RequestMapping(value="/addEmployee.html", method = RequestMethod.POST)
public ModelAndView add(
    @RequestParam("firstName")String firstName,
    @RequestParam("middleName")String middleName,
    @RequestParam("lastName")String lastName,
    @RequestParam("birthdate")String birthdate,
    @RequestParam("gender")String gender,
    @RequestParam("email")String email,
    @RequestParam("mobileNo")Long mobileNo,
    @RequestParam("adharNo")Long adharNo,
    @RequestParam("country")String country,
    @RequestParam("state")String state,
    @RequestParam("city")String city,
    @RequestParam("residentialAddress")String residentialAddress,
    @RequestParam("permanentAddress")String permanentAddress,
    @RequestParam("role")String role,
    @RequestParam("qualification")String qualification,
    @RequestParam("specialization")String specialization)
```
**Problem**: Same pattern as patient (14 parameters)
**Solution**: Create `EmployeeCreateDTO`

---

### 4. PRIMITIVE OBSESSION - Using primitives for domain concepts
**Issue**: Using String, int, float instead of small dedicated classes; loses domain meaning, adds boilerplate getters/setters  
**Treatment**: Replace Data Value with Object

#### Example 1: Patient.java - Primitive obsession with Name
**File**: src/main/java/com/project/entity/Patient.java (Lines 15-26)
```java
@Entity
public class Patient {
    private String pid;
    
    private Name name;  // GOOD - but created from primitives
    
    private String birthdate;        // SMELL - Should be LocalDate
    private String gender;           // SMELL - Should be Gender enum
    
    @Column(unique=true)
    private String emailID;          // SMELL - Should be Email value object
    
    @Column(unique=true)
    private long mobileNo;           // SMELL - Should be PhoneNumber value object
    
    @Column(unique=true)
    private long adharNo;            // SMELL - Should be AadharNumber value object
    
    private String country;          // SMELL - Part of Address (already has Address!)
    private String state;            // SMELL - Part of Address
    private String city;             // SMELL - Part of Address
    
    private Address address;
    
    private String bloodGroup;       // SMELL - Should be BloodType enum
    private String chronicDiseases;  // SMELL - Should be Disease[] or Set<Disease>
    private String medicineAllergy;  // SMELL - Should be Allergy value object
```

**Better Design**:
```java
@Entity
public class Patient {
    private String pid;
    private Name name;              // ✓ Already using object
    private LocalDate birthdate;    // ✓ Use proper date type
    private Gender gender;          // ✓ Use enum
    private Email email;            // ✓ Use value object
    private PhoneNumber mobileNo;   // ✓ Use value object
    private AadharNumber adharNo;   // ✓ Use value object
    private Address address;        // ✓ Already using object (contains country, state, city)
    private BloodType bloodGroup;   // ✓ Use enum
    private Set<Disease> diseases;  // ✓ Use Set of enums/objects
    private Allergy allergy;        // ✓ Use value object
}
```

#### Example 2: Controller parameters - Primitive obsession
**File**: src/main/java/com/project/controller/receptionist/AddPatientController.java (Lines 50-67)
```java
@RequestMapping(value="/addPatient.html", method = RequestMethod.POST)
public ModelAndView add(
    @RequestParam("firstName")String firstName,
    @RequestParam("middleName")String middleName,
    @RequestParam("lastName")String lastName,
    @RequestParam("email")String email,
    @RequestParam("mobileNo")Long mobileNo,
    @RequestParam("adharNo")Long adharNo,
    @RequestParam("bloodGroup")String bloodGroup,
    @RequestParam("chronicDiseases")String chronicDiseases,
    @RequestParam("medicineAllergy")String medicineAllergy,
    // ... more primitives
) {
    Name n1= new Name(firstName, middleName, lastName);
    Address a1= new Address(residentialAddress,permanentAddress);
    Patient p1= new Patient(n1,birthdate,gender,email,mobileNo,adharNo,country,
                           state,city,a1,bloodGroup,chronicDiseases,medicineAllergy,doctorId);
}
```

**Problem**: Lines 68-70 are manually converting primitives to objects  
**Better Design**:
```java
@PostMapping(value="/addPatient.html")
public ModelAndView add(@RequestBody PatientCreateDTO dto) {
    // PatientCreateDTO already has Name, Email, Phone, Address objects
    Patient patient = patientService.create(dto);
    return successView();
}

// DTO:
public class PatientCreateDTO {
    private Name name;              // Not separate firstName/middleName/lastName
    private LocalDate birthdate;
    private Gender gender;
    private Email email;            // Not String
    private PhoneNumber mobileNo;   // Not Long
    private AadharNumber adharNo;   // Not Long
    private Address address;
    private BloodType bloodGroup;   // Not String
}
```

#### Example 3: Employee.java - Same primitive obsession
**File**: src/main/java/com/project/entity/Employee.java (Lines 20-40)
```java
@Entity
public class Employee {
    @Temporal(TemporalType.DATE)
    private Date joiningDate;       // SMELL - Should be LocalDate
    
    private String eid;             // SMELL - Should be EmployeeId value object
    
    private Name name;              // ✓ Good
    
    private String birthdate;       // SMELL - Should be LocalDate
    private String gender;          // SMELL - Should be Gender enum
    
    @Column(unique=true)
    private String emailID;         // SMELL - Should be Email value object
    
    @Column(unique=true)
    private long mobileNo;          // SMELL - Should be PhoneNumber value object
    
    @Column(unique=true)
    private long adharNo;           // SMELL - Should be AadharNumber value object
    
    private String country;         // SMELL - Part of Address
    private String state;           // SMELL - Part of Address
    private String city;            // SMELL - Part of Address
    
    private Address address;        // ✓ Good
    
    private String role;            // SMELL - Should be Role enum
    private String qualification;   // SMELL - Should be Qualification object
    private String specialization;  // SMELL - Should be Specialization enum
}
```

---

### 5. DATA CLUMPS - Same group of variables appearing together
**Issue**: Same 2-4 variables appearing in multiple methods/classes signals missing abstraction  
**Treatment**: Extract Class or Introduce Parameter Object

#### Example 1: Name data clump
**Appears in**: Patient.java, Employee.java, and ALL Add/Edit Controllers

```java
// AddPatientController - Lines 68-70
Name n1= new Name(firstName, middleName, lastName);

// AddEmployeeController - Lines 59-60
Name n1= new Name(firstName, middleName, lastName);

// EditPatientController - Lines 74-75
Name n1=new Name(firstName,middleName,lastName);

// EditEmployeeController - Lines 88-89
Name n1=new Name(firstName,middleName,lastName);

// AND in Patient.java - Lines 50-69
public Patient(Name name, String birthdate, String gender, String emailID, long mobileNo,
        long adharNo, String country, String state, String city, Address address, String bloodGroup,
        String chronicDiseases, String medicineAllergy, String doctorId) {
    this.name = name;
    // ...
}

// AND in Employee.java - Lines 51-63
public Employee(Date joiningDate, Name name, String birthdate, String gender, String emailID, long mobileNo,
        long adharNo, String country, String state, String city, Address address, String role, String qualification,
        String specialization) {
    this.name = name;
    // ...
}
```

**Data Clump Identified**: `firstName, middleName, lastName` appears in:
- 4 controllers (4 times)
- 2 entity constructors (2 times)
- 2 entity field declarations (2 times)
- 2 DAO methods (2 times)
- **Total: 12+ places**

**Already Partially Solved**: Name class exists but not used consistently in parameters
**Full Solution**: Replace all `@RequestParam("firstName"), @RequestParam("middleName"), @RequestParam("lastName")` with `@RequestParam Name name`

#### Example 2: Address data clump
**Appears in**: Patient.java, Employee.java, and ALL Add/Edit Controllers

```java
// AddPatientController - Lines 69
Address a1= new Address(residentialAddress,permanentAddress);

// AddEmployeeController - Lines 60
Address a1= new Address(residentialAddress,permanentAddress);

// EditPatientController - Lines 75
Address a1= new Address(residentialAddress, permanentAddress);

// EditEmployeeController - Lines 89
Address a1= new Address(residentialAddress, permanentAddress);

// Patient.java constructor uses Address
// Employee.java constructor uses Address
// EditPatientDao includes address fields in query (Lines 32-44)
// EditEmployeeDao includes address fields in query
```

**Data Clump Identified**: `residentialAddress, permanentAddress` appears in:
- 4 controllers (4 times)
- 2 entities (2 times)
- 2 DAOs' queries (2 times)
- **Total: 8+ places**

**Already Partially Solved**: Address class exists  
**Full Solution**: Replace `@RequestParam("residentialAddress"), @RequestParam("permanentAddress")` with `@RequestParam Address address`

#### Example 3: Location data clump
**Appears in**: Patient.java, Employee.java, and ALL Add/Edit Controllers

```java
// Parameters scattered across all Add/Edit controllers:
@RequestParam("country")String country
@RequestParam("state")String state
@RequestParam("city")String city

// Patient.java - Lines 33-35
private String country;
private String state;
private String city;

// Employee.java - Lines 29-31
private String country;
private String state;
private String city;

// EditPatientDao.edit() - Lines 36-38
"country= :t7, state= :t8, city=:t9, " +

// EditEmployeeDao.edit() - Lines 36-38
"country= :t7, state= :t8, city=:t9, " +
```

**Problem**: Country, State, City appear together 10+ times but Location class doesn't exist!  
**Data Clump Identified**: `country, state, city`
- 4 controllers (12 parameters total)
- 2 entities (6 fields)
- 2 DAOs (6 query parameters)
- Already in Address but also duplicated as separate fields!

**Best Solution**: Move into Location object or consolidate in Address (currently Address only has residentialAddress, permanentAddress)
```java
// CREATE:
public class Address {
    private String residentialAddress;
    private String permanentAddress;
    private Location location;  // country, state, city
}

public class Location {
    private String country;
    private String state;
    private String city;
}

// OR BETTER: Merge with existing Address:
public class Address {
    private String residentialAddress;
    private String permanentAddress;
    private String country;
    private String state;
    private String city;
}

// Then remove duplicate fields from Patient/Employee:
@Entity
public class Patient {
    // REMOVE: private String country, state, city;
    private Address address;  // Address now contains all location info
}
```

#### Example 4: Personal info data clump
**Appears together in**: Patient.java, Employee.java constructors and controllers

```java
// Common pattern across all Add/Edit:
String firstName, String middleName, String lastName,  // Name
String birthdate,
String gender,
String email,
Long mobileNo,
Long adharNo,
String country, String state, String city,            // Location
String residentialAddress, String permanentAddress,   // Address

// This 13+ parameter clump appears in:
// - AddPatientController (Lines 50-67)
// - AddEmployeeController (Lines 46-55)
// - EditPatientController (Lines 62-76)
// - EditEmployeeController (Lines 70-84)
// - Patient constructor (Lines 50-69)
// - Employee constructor (Lines 51-63)
// - AddPatientDao.add() signature
// - EditPatientDao.edit() signature (Lines 24-30)
// - EditEmployeeDao.edit() signature
// - AddEmployeeDao.add() signature
```

**Data Clump Analysis**: Same 13+ variables appearing together ~15-20 times  
**Best Solution**: Create PersonDTO/PersonCreateDTO with all these fields grouped appropriately
```java
public class PersonDTO {
    private Name name;                  // Group 1: Identity
    private LocalDate birthdate;
    private Gender gender;
    private Email email;                // Group 2: Contact
    private PhoneNumber mobileNo;
    private AadharNumber adharNo;       // Group 3: ID
    private Address address;            // Group 4: Location (includes country, state, city)
}

// Base this for both Patient and Employee:
public class PatientCreateDTO extends PersonDTO {
    private BloodType bloodGroup;       // Patient-specific
    private String chronicDiseases;
    private String medicineAllergy;
}

public class EmployeeCreateDTO extends PersonDTO {
    private String role;
    private String qualification;
    private String specialization;
}
```

---

## CRITICAL ISSUES (Must Fix)

### 1. LoginController - Multiple Responsibilities
**File**: src/main/java/com/project/controller/LoginController.java (Lines 17-92)
- **Issue**: 6 different responsibilities in one class
- **Dependencies**: 3 DAOs (LoginDao, PatientPrescriptionDao, UsersInSystemDao)
- **Lines**: 92
- **Fix**: Create LoginService + use Facade pattern

### 2. EditPatientController - Extreme Feature Envy
**File**: src/main/java/com/project/controller/receptionist/EditPatientController.java
- **Issue**: 6 DAO dependencies for single resource
- **Parameters**: 17 parameters in edit() method
- **Fix**: Create PatientFacade, use DTOs

### 3. Switch Statement in AddOpdController
**File**: src/main/java/com/project/controller/opd/AddOpdController.java (Lines 35-60)
- **Issue**: Magic return codes (1/2/3) with if-else chain
- **Lines**: 26 lines of business logic in controller
- **Fix**: Use exception types or result objects, move to service

### 4. Duplicate Search DAOs (39.7% Duplication Root Cause)
**File**: src/main/java/com/project/dao/receptionist/SearchPatientDao.java
        src/main/java/com/project/dao/administrator/SearchEmployeeDao.java
- **Issue**: 8 methods with 95% identical code
- **Lines**: 80+ lines duplicated
- **Fix**: Create generic GenericSearchDao<T>

### 5. Divergent Change in Edit DAOs
**File**: EditPatientDao.java + EditEmployeeDao.java
- **Issue**: Same edit pattern duplicated, future changes need 2 edits
- **Lines**: 32 lines each, 90% identical
- **Fix**: Create AbstractEditDao<T> or use spring-data-jpa

---

## HIGH PRIORITY ISSUES

| # | Issue | Location | Impact | Fix |
|---|-------|----------|--------|-----|
| 6 | Long Method (50 lines) | AddOpdController.add() | Controller too complex | Extract to service |
| 7 | Long Method (44 lines) | EditPatientController.edit() | 17 parameters | Use DTO pattern |
| 8 | Long Method (32 lines) | EditPatientDao.edit() | Hardcoded queries, 15 params | Query builder + DTOs |
| 9 | Duplicate Try-Catch | All files (~50 instances) | Error handling scattered | Global exception handler + AOP |
| 10 | Logging Dependency | All 103 files | 100+ logActivities() calls | Use SLF4J instead of LoginDao |

---

## DISPENSABLES (Duplicate Code - 39.7% Duplicated)

```
Total Duplicate Lines: ~430-450 lines

Breakdown:
- Search methods (Try-Catch template):    60 lines
- Edit DAOs (Query building):             40 lines  
- Add DAOs (ID generation):               30 lines
- Controllers (Exception handling):      120 lines
- Entities (Getters/Setters):             80 lines
- View navigation (ModelAndView config): 100+ lines
```

**Quick Wins** (Easiest to fix):
1. SearchPatientDao + SearchEmployeeDao → GenericSearchDao
2. EditPatientDao + EditEmployeeDao → AbstractEditDao
3. AddPatientDao + AddEmployeeDao → AbstractAddDao
4. Patient + Employee entities → PersonEntity base class

---

## COUPLERS (Tight Coupling Issues)

### Feature Envy Examples:
```
LoginController → {LoginDao, PatientPrescriptionDao, UsersInSystemDao}
    (Should be: LoginController → LoginService → DAOs)

EditPatientController → {PatientDetailsDao, SearchPatientDao, EditPatientDao, 
                         AddPatientDao, PatientPrescriptionDao, LoginDao}
    (Should be: EditPatientController → PatientFacade → PatientService)
```

### Message Chains (Law of Demeter Violations):
```
// BAD (Line 38 in EditPatientController):
String doctorname=dao3.searchDoctorAssigned(temp.getDoctorId());

// GOOD:
PatientInfo info = patientFacade.getEditInfo(pid);
```

---

## OO ABUSERS (Design Pattern Violations)

### 1. Switch Statement Pattern
```java
// AddOpdController.add() - Lines 45-64
int b=dao.add(q1);
if(b==1) { success case }
else if(b==2) { error case 1 }
else if(b==3) { error case 2 }
```
**Fix**: Use exception types or Result<T> pattern

### 2. Refused Bequest
```java
// Patient.java - Lines 1-150
// Employee.java - Lines 1-150
// 70% code duplication - should share PersonEntity base class
```

---

## CHANGE PREVENTERS (Divergent Change)

### Adding new field to Patient/Employee requires changes in 6+ places:

```
1. Update Patient.java entity
2. Update EditPatientDao query + setParameter
3. Update EditPatientController.edit() parameters
4. Update AddPatientController.add() parameters
5. Same for Employee (5 more changes)
TOTAL: 10 changes just to add 1 field!
```

**Should be**: 1 change in entity, 1 in service

---

## RECOMMENDED REFACTORING SEQUENCE

### Phase 1 (Week 1) - CREATE SERVICE LAYER
```
1. Create PatientService
2. Create EmployeeService  
3. Create LoginService
4. Move business logic from DAOs to Services
```

### Phase 2 (Week 2) - EXTRACT DTOs & ELIMINATE PARAMETERS
```
1. Create PatientDTO (replace 17 parameters)
2. Create EmployeeDTO (replace 16 parameters)
3. Update controllers to use DTOs
```

### Phase 3 (Week 3) - ELIMINATE DUPLICATION
```
1. Create GenericSearchDao<T>
2. Create AbstractEditDao<T>
3. Create AbstractAddDao<T>
4. Create PersonEntity base class
```

### Phase 4 (Week 4) - LOGGING & EXCEPTION HANDLING
```
1. Replace LoginDao.logActivities() with SLF4J
2. Create global exception handler
3. Use @ControllerAdvice for error handling
```

---

## QUICK METRICS

| Metric | Current | Target | Effort |
|--------|---------|--------|--------|
| Code Duplication | 39.7% | <15% | High |
| Avg Parameters/Method | 17 | <5 | High |
| Avg Method Lines | 45 | <20 | High |
| Classes with 1 Responsibility | 20% | 90% | High |
| DAO Dependencies/Controller | 3-6 | 1 | High |
| Long Methods (>50 lines) | 10+ | 0 | High |

---

## DETAILED REPORT LOCATION
📄 **Full Analysis**: [CODE_SMELL_ANALYSIS.md](CODE_SMELL_ANALYSIS.md)

Contains:
- 25 individual code smell issues with exact line numbers
- Complete code examples showing violations
- SRP explanations for each violation
- Root cause analysis
- Refactoring recommendations
- Code duplication hotspots mapping
