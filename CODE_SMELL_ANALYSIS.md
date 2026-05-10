# Hospital Management System - Code Smell Analysis Report
**Scope**: Spring MVC + Hibernate ORM Application  
**Code Duplication**: 39.7% (HIGH RISK)  
**Analysis Date**: May 2026

---

## 1. BLOATERS - Code Smell Analysis

### 1.1 LONG METHODS (>50 lines)

#### Issue 1: AddOpdController.add() - LONG METHOD with SWITCH-LIKE LOGIC
**File**: [src/main/java/com/project/controller/opd/AddOpdController.java](src/main/java/com/project/controller/opd/AddOpdController.java#L26-L75)  
**Lines**: 26-75 (50 lines)  
**Violation**: Switch statement pattern disguised as if-else chain  

```java
public ModelAndView add(@RequestParam("pid")String pid) {
    // ... initialization (lines 26-39)
    if(! doctorid.equals(null)) {                    // Line 39
        Opd q1= new Opd(pid, doctorid, Opd.PENDING);
        int b=dao.add(q1);
        
        if(b==1) {                                   // Line 45 - START OF SWITCH LOGIC
            // Handle success
        }
        else if(b==2) {                              // Line 52
            // Handle duplicate patient error
        }
        else if(b==3) {                              // Line 58
            // Handle doctor unavailable error
        }
        else {                                       // Line 64
            throw new Exception();
        }
    }
}
```

**Why it's a Smell (SRP Violation)**:
- Controller handles 4 different return scenarios based on DAO return codes
- Mixing business logic (error handling) with presentation logic
- Future additions require modifying this method
- **SRP Violation**: Method violates Single Responsibility - should only handle request→response, not business logic branching

---

#### Issue 2: EditPatientController - OVERLOADED METHOD with LONG PARAMETER LIST  
**File**: [src/main/java/com/project/controller/receptionist/EditPatientController.java](src/main/java/com/project/controller/receptionist/EditPatientController.java#L62-L105)  
**Lines**: 62-105 (44 lines)  
**Parameter Count**: 17 parameters  
**Related Method**: Lines 35-56 (22 lines) - same method name with different params  

```java
@RequestMapping(value="/editPatient.html", method = RequestMethod.POST)
public ModelAndView edit(@RequestParam("pid")String pid, 
    @RequestParam("firstName")String firstName,           // Param 1
    @RequestParam("middleName")String middleName,         // Param 2
    @RequestParam("lastName")String lastName,             // Param 3
    @RequestParam("birthdate")String birthdate,           // Param 4
    @RequestParam("gender")String gender,                 // Param 5
    @RequestParam("email")String email,                   // Param 6
    @RequestParam("mobileNo")Long mobileNo,               // Param 7
    @RequestParam("adharNo")Long adharNo,                 // Param 8
    @RequestParam("country")String country,               // Param 9
    @RequestParam("state")String state,                   // Param 10
    @RequestParam("city")String city,                     // Param 11
    @RequestParam("residentialAddress")String residentialAddress,  // Param 12
    @RequestParam("permanentAddress")String permanentAddress,      // Param 13
    @RequestParam("bloodGroup")String bloodGroup,         // Param 14
    @RequestParam("chronicDiseases")String chronicDiseases, // Param 15
    @RequestParam("medicineAllergy")String medicineAllergy,   // Param 16
    @RequestParam("doctorId")String doctorId)             // Param 17
```

**Why it's a Smell**:
- Method has 17 parameters exceeding acceptable limits
- Contains logging statement (Line 78) mixing concerns
- **SRP Violation**: Controller mixed with DTO creation logic (Lines 74-75)
- Hard to test - requires mocking all 17 parameters
- **Impact**: Changes to patient fields cascade to controller signature

---

#### Issue 3: EditEmployeeController.edit() - DUPLICATE LONG METHOD  
**File**: [src/main/java/com/project/controller/administrator/EditEmployeeController.java](src/main/java/com/project/controller/administrator/EditEmployeeController.java#L72-L104)  
**Lines**: 72-104 (33 lines, similar to EditPatientController)  
**Parameter Count**: 16 parameters  

```java
public ModelAndView edit(@RequestParam("eid")String eid, 
    @RequestParam("firstName")String firstName,
    @RequestParam("middleName")String middleName,
    // ... 13 more parameters (Lines 73-88)
    @RequestParam("specialization")String specialization)
{
    try {
        Name n1=new Name(firstName,middleName,lastName);
        Address a1= new Address(residentialAddress, permanentAddress);
        // Logging business data (Line 91) - SRP VIOLATION
        int res=dao2.edit(eid,n1,birthdate,gender,email,mobileNo,
                         adharNo,country,state,city,a1,role,
                         qualification,specialization);
        // ... 3 different response types based on result
    }
}
```

**Why it's a Smell**:
- **CODE DUPLICATION**: Nearly identical to EditPatientController pattern
- Same 17+ parameter antipattern
- Same error handling logic (try-catch wrapping)
- **SRP Violation**: Controller should not build entity objects or handle all validation

---

#### Issue 4: EditPatientDao.edit() - LONG METHOD WITH HARDCODED QUERIES  
**File**: [src/main/java/com/project/dao/receptionist/EditPatientDao.java](src/main/java/com/project/dao/receptionist/EditPatientDao.java#L24-L55)  
**Lines**: 24-55 (32 lines)  
**Parameters**: 15 parameters  

```java
@Transactional
public int edit(String pid, Name name, String birthdate, String gender, 
    String emailId, Long mobileNo, Long adharNo, String country, 
    String state, String city, Address address, String bloodGroup, 
    String chronicDiseases, String medicineAllergy, String doctorId) 
{
    infoLog.logActivities(...);  // Line 28 - Logging mixed into DAO
    
    Session session= sf.getCurrentSession();
    Query q1=session.createQuery("update Patient set " +
        "name.firstName= :t1, name.middleName= :t2, name.lastName= :t3, " +
        // 10 more setParameter calls (Lines 33-44)
    );
    
    q1.setParameter("t1", name.getFirstName());
    q1.setParameter("t2", name.getMiddleName());
    // ... 13 MORE setParameter calls with hardcoded :t1, :t2, :t3 placeholders
    q1.setParameter("id", pid);
    
    try{
        int res= q1.executeUpdate();
        return res;
    }
    catch(Exception e) { return 0; }
}
```

**Why it's a Smell**:
- **Hardcoded Query Parameters**: Uses :t1, :t2, :t3 pattern (confusing, not maintainable)
- **Logging in DAO**: Line 28 - logActivities() mixes concerns
- **Exception Suppression**: Line 54 - catch returns 0 hiding actual error
- **SRP Violation**: DAO only updates Patient, but receives 15 parameters to build query
- **Divergent Change**: Any Patient field addition requires modifying both the query string AND setParameter calls

---

#### Issue 5: EditEmployeeDao.edit() - DUPLICATE LONG METHOD  
**File**: [src/main/java/com/project/dao/administrator/EditEmployeeDao.java](src/main/java/com/project/dao/administrator/EditEmployeeDao.java#L25-L57)  
**Lines**: 25-57 (33 lines)  
**Parameters**: 14 parameters  

```java
@Transactional
public int edit(String eid, Name name, String birthdate, String gender, 
    String emailId, Long mobileNo, Long adharNo, String country, 
    String state, String city, Address address, String role, 
    String qualification, String specialization) 
{
    infoLog.logActivities("in EditEmployeeDao-edit: got= "...);  // LOGGING IN DAO
    
    Session session= sf.getCurrentSession();
    Query q1=session.createQuery("update Employee set " +
        "name.firstName= :t1, name.middleName= :t2, name.lastName= :t3, " +
        "birthdate= :t4, emailId= :t5, mobileNo= :t6, " +
        // Similar pattern to EditPatientDao
        "qualification= :t12, specialization= :t13 where eid= :id");
    // ... 13 setParameter calls
    
    try {
        int res= q1.executeUpdate();
        return res;
    }
    catch(Exception e) {
        infoLog.logActivities("in logindao-validate: "+e);  // WRONG CLASS NAME IN LOG!
        return 0;
    }
}
```

**Why it's a Smell**:
- **EXACT DUPLICATE PATTERN**: Identical to EditPatientDao.edit() structure
- **Logging in DAO**: Line 30 violates SRP
- **Copy-Paste Error**: Line 52 still says "logindao-validate" instead of "EditEmployeeDao-edit"
- **Exception Swallowing**: Returns 0, logs nothing specific
- **Divergent Change**: Future modifications must happen in both DAOs

---

#### Issue 6: AddPatientDao.add() - LONG METHOD WITH MIXED CONCERNS  
**File**: [src/main/java/com/project/dao/receptionist/AddPatientDao.java](src/main/java/com/project/dao/receptionist/AddPatientDao.java#L56-L94)  
**Lines**: 56-94 (39 lines)  

```java
@Transactional
public boolean add(Patient p1) 
{
    infoLog.logActivities("in AddPatientDao-add: got= "+p1);  // LOGGING
    Date date= new Date();
    p1.setRegistrationDate(date);           // Line 61 - Business logic
    
    Session session= sf.getCurrentSession();
    session.save(p1);
    
    // START OF ID GENERATION LOGIC - SHOULD BE SEPARATE
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

**Why it's a Smell**:
- **SRP Violation**: DAO handles patient save AND ID generation logic
- **Logging Pollution**: 3 separate logActivities() calls
- **Business Logic in DAO**: Lines 60-61, 72 (ID incrementing)
- **Hidden Dependencies**: Depends on IdGenerate table structure
- **No Error Handling**: Boolean return doesn't indicate which step failed

---

### 1.2 LARGE CLASSES (Multiple Responsibilities)

#### Issue 7: LoginController - TOO MANY RESPONSIBILITIES  
**File**: [src/main/java/com/project/controller/LoginController.java](src/main/java/com/project/controller/LoginController.java)  
**Lines**: 17-92  

```java
@Controller
public class LoginController 
{
    @Autowired
    LoginDao dao;                          // Responsibility 1: Handle login validation
    
    @Autowired
    PatientPrescriptionDao dao1;           // Responsibility 2: Display prescription counts
    
    @Autowired
    UsersInSystemDao dao2;                 // Responsibility 3: Track users online
    
    @RequestMapping(value="/login.html", method = RequestMethod.POST)
    public ModelAndView view() {           // Responsibility 4: Display login form
        // ...
    }
    
    @RequestMapping(value="/dashboard.html", method = RequestMethod.POST)
    public ModelAndView validate(...) {    // Responsibility 5: Validate credentials
        // ...
        mv.addObject("prescriptionsCount", dao1.prescriptionPrintCount());  
        mv.addObject("users_count", dao2.getUsersInSystem());
        // Responsibility 6: Determine role-based dashboard content
        return mv;
    }
}
```

**Why it's a Smell**:
- **6 Different Responsibilities**: Login form display, validation, role resolution, prescription counting, user tracking, session management
- **Feature Envy**: Depends on 3 different DAOs (PatientPrescriptionDao, UsersInSystemDao, LoginDao)
- **SRP Violation**: Should only validate login, not determine dashboard content
- **Tight Coupling**: Directly coupled to data layer decisions
- **Hard to Test**: All 3 DAOs must be mocked for testing

---

#### Issue 8: EditPatientController - LARGE CLASS WITH MANY DEPENDENCIES  
**File**: [src/main/java/com/project/controller/receptionist/EditPatientController.java](src/main/java/com/project/controller/receptionist/EditPatientController.java)  
**Dependencies**: 6 autowired components  

```java
@Controller 
public class EditPatientController 
{
    @Autowired
    PatientDetailsDao dao1;        // Get patient details
    @Autowired
    SearchPatientDao dao3;         // Search patient by doctor ID
    @Autowired
    EditPatientDao dao2;           // Edit patient
    @Autowired
    AddPatientDao dao4;            // Get doctors list
    @Autowired
    PatientPrescriptionDao dao5;   // Get prescription count
    @Autowired
    LoginDao infoLog;              // Logging
    
    // Multiple methods handling different aspects of patient editing
    @RequestMapping(value="/editPatientView.html", method=RequestMethod.POST)
    public ModelAndView edit(@RequestParam("pid")String pid) { }
    
    @RequestMapping(value="/editPatient.html", method = RequestMethod.POST)
    public ModelAndView edit(...17 parameters...) { }
}
```

**Why it's a Smell**:
- **Large Class**: 6 dependencies for a single resource (Patient editing)
- **SRP Violation**: Multiple responsibilities (fetch, search, edit, list doctors, count prescriptions)
- **Coupling**: Each method requires different DAO combinations
- **Naming Duplication**: Two methods with same name "edit" (overloading)
- **Feature Envy**: Doesn't own the data but orchestrates 4 different operations

---

#### Issue 9: SearchPatientDao - DUPLICATE METHODS (Dispensable)  
**File**: [src/main/java/com/project/dao/receptionist/SearchPatientDao.java](src/main/java/com/project/dao/receptionist/SearchPatientDao.java)  

```java
@Component
public class SearchPatientDao 
{
    @Transactional
    public Patient searchName(String firstName, String lastName) {
        infoLog.logActivities("in SearchPatientDao-searchName: got= "...);
        Session session= sf.getCurrentSession();
        Query q1=session.createQuery("from Patient where firstName= :f AND lastName= :l");
        // TRY-CATCH PATTERN DUPLICATED BELOW
    }
    
    @Transactional
    public Patient searchId(String pid) {
        infoLog.logActivities("in SearchPatientDao-searchId: got= "...);
        Session session= sf.getCurrentSession();
        Query q1=session.createQuery("from Patient where pid= :id");
        // IDENTICAL TRY-CATCH PATTERN
    }
    
    @Transactional
    public Patient searchMobileNo(Long mobileNo) {
        infoLog.logActivities("in SearchPatientDao-searchMobileNo: got= "...);
        Session session= sf.getCurrentSession();
        Query q1=session.createQuery("from Patient where mobileNo= :no");
        // IDENTICAL TRY-CATCH PATTERN
    }
    
    @Transactional
    public Patient searchAdharNo(Long adharNo) {
        infoLog.logActivities("in SearchPatientDao-searchAdharNo: got= "...);
        // CONTINUES WITH IDENTICAL PATTERN
    }
}
```

**Why it's a Smell**:
- **Duplicate Code (~39.7% reported duplication fits here)**
- **SRP Violation**: Generic search abstraction missing
- **Template Method Anti-pattern**: Repeats session/query/execute/exception/log pattern
- **Maintenance Issue**: Bug fixes must be applied to 4 methods
- **Logging Pollution**: Each method logs redundantly

---

#### Issue 10: SearchEmployeeDao - DUPLICATE STRUCTURE  
**File**: [src/main/java/com/project/dao/administrator/SearchEmployeeDao.java](src/main/java/com/project/dao/administrator/SearchEmployeeDao.java)  

```java
@Component
public class SearchEmployeeDao 
{
    @Transactional
    public Employee searchName(String firstName, String lastName) {
        // IDENTICAL TO SearchPatientDao.searchName PATTERN
    }
    
    @Transactional
    public Employee searchId(String id) {
        // IDENTICAL PATTERN
    }
    
    @Transactional
    public Employee searchMobileNo(String mobileNo) {
        // IDENTICAL PATTERN
    }
    
    @Transactional
    public Employee searchAadharNo(String aadharNo) {
        // IDENTICAL PATTERN CONTINUES...
    }
}
```

**Why it's a Smell**:
- **EXACT DUPLICATE CLASS**: Copy-pasted from SearchPatientDao
- **Same 4-search methods** with only entity type changed
- **Divergent Change Risk**: Bug in SearchPatientDao won't be fixed in SearchEmployeeDao
- **Contributing to 39.7% Duplication**: Both classes are 80%+ duplicated

---

---

## 2. OO ABUSERS - Code Smell Analysis

### 2.1 SWITCH STATEMENTS (Logic Duplication)

#### Issue 11: AddOpdController.add() - SWITCH STATEMENT PATTERN  
**File**: [src/main/java/com/project/controller/opd/AddOpdController.java](src/main/java/com/project/controller/opd/AddOpdController.java#L35-L60)  
**Lines**: 35-60  

```java
public ModelAndView add(@RequestParam("pid")String pid) {
    // ... setup code ...
    
    int b=dao.add(q1);
    
    // SWITCH STATEMENT DISGUISED AS IF-ELSE
    if(b==1) {                                               // Line 35
        ModelAndView mv= new ModelAndView();
        mv.setViewName("successPage");
        mv.addObject("prescriptionsCount", dao1.prescriptionPrintCount());
        return mv;
    }
    else if(b==2) {                                          // Line 41
        infoLog.logActivities("in AddOpdController-add: ");
        ModelAndView mv= new ModelAndView();
        mv.setViewName("failure");
        mv.addObject("error","<b>patient is aldready added in OPD queue</b>");
        return mv;
    }
    else if(b==3) {                                          // Line 48
        infoLog.logActivities("in AddOpdController-add: ");
        ModelAndView mv= new ModelAndView();
        mv.setViewName("failure");
        mv.addObject("error","<b>Your assigned doctor is not available...plz choose another doctor and then try again</b>");
        return mv;
    }
    else {                                                   // Line 55
        throw new Exception();
    }
}
```

**Why it's a Smell**:
- **Magic Numbers**: Return codes 1, 2, 3 have implicit meanings
- **SRP Violation**: Controller contains business logic (error type determination)
- **Violates Open/Closed Principle**: Adding new error type requires modifying this method
- **Feature Envy**: Controller depends on knowing DAO's return code semantics
- **No Polymorphism**: Should use exception types or result objects instead
- **Difficult to Test**: Each branch requires separate test case

---

### 2.2 REFUSED BEQUEST (Incomplete Inheritance)

#### Issue 12: Entity Classes - MISSING PROPER INHERITANCE STRUCTURE  
**File**: [src/main/java/com/project/entity/Patient.java](src/main/java/com/project/entity/Patient.java#L1-150)  
**File**: [src/main/java/com/project/entity/Employee.java](src/main/java/com/project/entity/Employee.java#L1-150)  

```java
// Patient Entity - 14 fields including personal info
@Entity
public class Patient {
    private String pid;
    private Name name;
    private String birthdate;
    private String gender;
    private String emailID;
    private long mobileNo;
    private long adharNo;
    private String country;
    private String state;
    private String city;
    private Address address;
    private String bloodGroup;
    private String chronicDiseases;
    private String medicineAllergy;
    private String doctorId;
    // ... 20+ getters/setters
}

// Employee Entity - 13 fields MOSTLY IDENTICAL to Patient
@Entity
public class Employee {
    private String eid;
    private Name name;                  // SAME
    private String birthdate;           // SAME
    private String gender;              // SAME
    private String emailID;             // SAME
    private long mobileNo;              // SAME
    private long adharNo;               // SAME
    private String country;             // SAME
    private String state;               // SAME
    private String city;                // SAME
    private Address address;            // SAME
    private String role;
    private String qualification;
    private String specialization;
    // ... 20+ identical getters/setters
}
```

**Why it's a Smell**:
- **Refused Bequest**: Both classes should inherit from a common `Person` base class
- **Code Duplication**: 9 common fields duplicated in both entities
- **Copy-Paste Inheritance**: Created by copying entire class structure
- **Maintenance Risk**: Change to `Address` component requires updating both entities
- **SRP Violation**: Each entity handles its own field getters/setters redundantly
- **Contributing to 39.7% Duplication**: 70%+ of fields are identical

---

#### Issue 13: AddPatientController & AddEmployeeController - REFUSED BEQUEST  
**File**: [src/main/java/com/project/controller/receptionist/AddPatientController.java](src/main/java/com/project/controller/receptionist/AddPatientController.java#L50-L110)  
**File**: [src/main/java/com/project/controller/administrator/AddEmployeeController.java](src/main/java/com/project/controller/administrator/AddEmployeeController.java#L49-L95)  

```java
// AddPatientController
@RequestMapping(value="/addPatient.html", method = RequestMethod.POST)
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
    @RequestParam("bloodGroup")String bloodGroup,
    @RequestParam("chronicDiseases")String chronicDiseases,
    @RequestParam("medicineAllergy")String medicineAllergy,
    @RequestParam("doctorId")String doctorId
)

// AddEmployeeController - IDENTICAL STRUCTURE
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
    @RequestParam("specialization")String specialization
)
```

**Why it's a Smell**:
- **Refused Bequest**: Both controllers should inherit from a base `AddEntityController<T>`
- **Copy-Paste Error**: Structure duplicated exactly except for 2-3 domain-specific fields
- **SRP Violation**: Both override add() identically
- **Violates DRY**: Same error handling, same try-catch pattern
- **Contributing to 39.7% Duplication**: Both classes are 90%+ identical

---

---

## 3. CHANGE PREVENTERS - Code Smell Analysis

### 3.1 DIVERGENT CHANGE

#### Issue 14: EditPatientDao vs EditEmployeeDao - DIVERGENT CHANGE PATTERN  
**File 1**: [src/main/java/com/project/dao/receptionist/EditPatientDao.java](src/main/java/com/project/dao/receptionist/EditPatientDao.java)  
**File 2**: [src/main/java/com/project/dao/administrator/EditEmployeeDao.java](src/main/java/com/project/dao/administrator/EditEmployeeDao.java)  

```java
// EditPatientDao - Lines 24-55
public int edit(String pid, Name name, String birthdate, String gender,
    String emailId, Long mobileNo, Long adharNo, String country,
    String state, String city, Address address, String bloodGroup,
    String chronicDiseases, String medicineAllergy, String doctorId)
{
    Session session= sf.getCurrentSession();
    Query q1=session.createQuery("update Patient set " +
        "name.firstName= :t1, name.middleName= :t2, name.lastName= :t3, " +
        "birthdate= :t4, emailId= :t5, mobileNo= :t6, " +
        "country= :t7, state= :t8, city=:t9, " +
        "address.residentialAddress= :t10, chronicDiseases= :t13, " +
        "medicineAllergy= :t14, doctorId= :t15 where pid= :id");
    // 15 setParameter calls (Lines 35-44)
}

// EditEmployeeDao - Lines 25-57
public int edit(String eid, Name name, String birthdate, String gender,
    String emailId, Long mobileNo, Long adharNo, String country,
    String state, String city, Address address, String role,
    String qualification, String specialization)
{
    Session session= sf.getCurrentSession();
    Query q1=session.createQuery("update Employee set " +
        "name.firstName= :t1, name.middleName= :t2, name.lastName= :t3, " +
        "birthdate= :t4, emailId= :t5, mobileNo= :t6, " +
        "country= :t7, state= :t8, city=:t9, " +
        "address.residentialAddress= :t10, qualification= :t12, " +
        "specialization= :t13 where eid= :id");
    // 13 setParameter calls
}
```

**Why it's a Smell (Divergent Change)**:
- **Different Update Queries**: Same entity pattern, different fields to update
- **Future Requirement**: If we add a new common field (e.g., `verified` flag):
  - Must update Patient entity
  - Must update EditPatientDao query + setParameter
  - Must update Employee entity
  - Must update EditEditployeeDao query + setParameter
  - Must update both controllers' parameter lists
  - Results in 6-10 changes across files
- **Fragmented Maintenance**: No single place to change common edit logic
- **Violation of DRY**: Query structure duplicated with slight variations
- **Future Pain Point**: Any framework upgrade or database changes require parallel modifications

---

#### Issue 15: AddPatientDao vs AddEmployeeDao - DIVERGENT CHANGE IN ID GENERATION  
**File 1**: [src/main/java/com/project/dao/receptionist/AddPatientDao.java](src/main/java/com/project/dao/receptionist/AddPatientDao.java#L56-94)  
**File 2**: [src/main/java/com/project/dao/administrator/AddEmployeeDao.java](src/main/java/com/project/dao/administrator/AddEmployeeDao.java#L28-65)  

```java
// AddPatientDao - ID generation logic (Lines 73-86)
Query q1=session.createQuery(" from IdGenerate");
IdGenerate temp= (IdGenerate) q1.uniqueResult();
int pid=temp.getPid();
infoLog.logActivities("in AddPatientDao-add: pid= "+pid);
pid++;
q1=session.createQuery("update IdGenerate set pid= :i");
q1.setParameter("i", pid);
int res= q1.executeUpdate();

// AddEmployeeDao - ID generation logic (Lines 48-60)
Query q1=session.createQuery(" from IdGenerate");
IdGenerate temp= (IdGenerate) q1.uniqueResult();
int eid=temp.getEid();
eid++;
q1=session.createQuery("update IdGenerate set eid= :i");
q1.setParameter("i", eid);
int res= q1.executeUpdate();
```

**Why it's a Smell**:
- **Duplicated Business Logic**: ID generation logic copied in 2 places
- **Divergent Change**: If ID generation strategy changes (e.g., UUID, snowflake):
  - Must update 2 DAOs independently
  - Inconsistency risk (one updated, one forgotten)
  - No single source of truth
- **Contributing to 39.7% Duplication**: Same sequence duplicated exactly
- **SRP Violation**: DAOs shouldn't contain ID generation logic (should be service)
- **Testability**: Can't unit test ID generation separately

---

#### Issue 16: SearchPatientDao vs SearchEmployeeDao - DIVERGENT SEARCH METHODS  
**File 1**: [src/main/java/com/project/dao/receptionist/SearchPatientDao.java](src/main/java/com/project/dao/receptionist/SearchPatientDao.java)  
**File 2**: [src/main/java/com/project/dao/administrator/SearchEmployeeDao.java](src/main/java/com/project/dao/administrator/SearchEmployeeDao.java)  

Both DAOs contain 4 identical search methods (searchName, searchId, searchMobileNo, searchAdharNo):

```java
// Pattern repeated 4 times in EACH DAO

// SearchPatientDao.searchName() (Lines 19-39)
public Patient searchName(String firstName, String lastName) {
    infoLog.logActivities("in SearchPatientDao-searchName: got= "+firstName+" "+lastName);
    Session session= sf.getCurrentSession();
    Query q1=session.createQuery("from Patient where firstName= :f AND lastName= :l");
    q1.setParameter("f", firstName);
    q1.setParameter("l", lastName);
    try {
        Patient temp= (Patient) q1.uniqueResult();
        infoLog.logActivities("in SearchPatientDao-searchName: found= "+temp);
        return temp;
    }
    catch(Exception e) {
        infoLog.logActivities("in SearchPatientDao-searchName: "+e);
        return null;
    }
}

// SearchEmployeeDao.searchName() (Lines 27-49)
// IDENTICAL STRUCTURE - just using Employee instead of Patient
```

**Why it's a Smell**:
- **8 Methods (4 in each DAO) with ~95% identical code**
- **Future Change Example**: If logging format changes, must update 8 methods
- **If Exception handling changes**: Update 8 places
- **If Query structure changes**: Update 8+ places
- **Maintenance Nightmare**: Any enhancement scattered across multiple files
- **Contributing Heavily to 39.7% Duplication**

---

---

## 4. DISPENSABLES - Code Smell Analysis

### 4.1 DUPLICATE CODE

#### Issue 17: Repetitive Try-Catch-Log Pattern in Search DAOs  
**File**: [src/main/java/com/project/dao/receptionist/SearchPatientDao.java](src/main/java/com/project/dao/receptionist/SearchPatientDao.java#L19-70)  

```java
// Pattern 1 (searchName) - Lines 18-39
@Transactional
public Patient searchName(String firstName, String lastName) {
    infoLog.logActivities("in SearchPatientDao-searchName: got= "+firstName+" "+lastName);
    Session session= sf.getCurrentSession();
    Query q1=session.createQuery("from Patient where firstName= :f AND lastName= :l");
    q1.setParameter("f", firstName);
    q1.setParameter("l", lastName);
    try {
        Patient temp= (Patient) q1.uniqueResult();
        infoLog.logActivities("in SearchPatientDao-searchName: found= "+temp);
        return temp;
    }
    catch(Exception e) { 
        infoLog.logActivities("in SearchPatientDao-searchName: "+e);
        return null;
    }
}

// Pattern 2 (searchId) - Lines 42-62 - IDENTICAL EXCEPT:
@Transactional
public Patient searchId(String pid) {
    infoLog.logActivities("in SearchPatientDao-searchId: got= "+pid);  // Different method name
    Session session= sf.getCurrentSession();
    Query q1=session.createQuery("from Patient where pid= :id");        // Different query
    q1.setParameter("id", pid);
    try {
        Patient temp= (Patient) q1.uniqueResult();
        infoLog.logActivities("in SearchPatientDao-searchId: found= "+temp);  // Different method name
        return temp;
    }
    catch(Exception e) { 
        infoLog.logActivities("in SearchPatientDao-searchId: "+e);  // Different method name
        return null;
    }
}

// Pattern 3 (searchMobileNo) - SAME - Lines 65-82
// Pattern 4 (searchAdharNo) - SAME - Lines 85-...
```

**Duplicate Code Analysis**:
- **Same Try-Catch Block**: Repeated 4 times in one class
- **Same Session Management**: `sf.getCurrentSession()` in all 4 methods
- **Same Log Pattern**: `infoLog.logActivities()` before and after
- **Same Error Handling**: All return null on exception
- **Only Variations**: Method name and Query WHERE clause
- **Code Duplication Contribution**: ~40-50 lines duplicated across 4 methods
- **Could be Reduced**: Generic search method would cut code by 70%

---

#### Issue 18: Duplicate Try-Catch Pattern in All DAOs  
**Pattern Seen In**:
- AddPatientDao.add() - Lines 56-94
- AddEmployeeDao.add() - Lines 28-66
- EditPatientDao.edit() - Lines 24-55
- EditEmployeeDao.edit() - Lines 25-57
- SearchPatientDao (all 4 methods)
- SearchEmployeeDao (all 4 methods)
- All controller methods

```java
// GLOBAL PATTERN - Repeated ~50+ times across codebase
try {
    // ... business logic ...
    infoLog.logActivities("in [ClassName]-[methodName]: [result]");
    return successResult;
}
catch(Exception e) {
    infoLog.logActivities("in [ClassName]-[methodName]: "+e);
    // Generic catch suppresses error details
    return failureResult;  // Often returns null, 0, or false
}
```

**Duplicate Code Analysis**:
- **Logged 50+ times** across 103 Java files
- **Always Generic Catch**: Doesn't differentiate exception types
- **Always Logs to Same Sink**: infoLog.logActivities()
- **Always Returns Same Failure Value**: null, 0, or false regardless of error type
- **Contributing to 39.7% Duplication**: Each try-catch block is ~8 lines duplicated
- **Code Smell Compounded**: Added across all 103 files = 400-600 lines of duplicate exception handling
- **Should Use**: Global exception handler or custom exception types

---

#### Issue 19: Duplicate Parameter Mapping in Controllers  
**File**: [src/main/java/com/project/controller/receptionist/AddPatientController.java](src/main/java/com/project/controller/receptionist/AddPatientController.java#L57-60)  
**File**: [src/main/java/com/project/controller/receptionist/EditPatientController.java](src/main/java/com/project/controller/receptionist/EditPatientController.java#L74-75)  
**File**: [src/main/java/com/project/controller/administrator/AddEmployeeController.java](src/main/java/com/project/controller/administrator/AddEmployeeController.java#L59-60)  
**File**: [src/main/java/com/project/controller/administrator/EditEmployeeController.java](src/main/java/com/project/controller/administrator/EditEmployeeController.java#Line 88-89)  

```java
// AddPatientController - Lines 57-60
Name n1= new Name(firstName, middleName, lastName);
Address a1= new Address(residentialAddress, permanentAddress);
infoLog.logActivities("in AddPatientController-add: got= "+n1+" "+birthdate+" "...);
Patient p1= new Patient(n1,birthdate,gender,email,mobileNo,adharNo,country,
                        state,city,a1,bloodGroup,chronicDiseases,medicineAllergy,doctorId);

// EditPatientController - Lines 74-75 - IDENTICAL
Name n1=new Name(firstName,middleName,lastName);
Address a1= new Address(residentialAddress, permanentAddress);

// AddEmployeeController - Lines 59-60 - IDENTICAL
Name n1= new Name(firstName, middleName, lastName);
Address a1= new Address(residentialAddress,permanentAddress);

// EditEmployeeController - Lines 88-89 - IDENTICAL
Name n1=new Name(firstName,middleName,lastName);
Address a1= new Address(residentialAddress, permanentAddress);
```

**Duplicate Code Analysis**:
- **Same Entity Creation Logic**: Repeated 4+ times
- **Same Parameter Extraction**: All extract firstName, middleName, lastName
- **Should Use**: DTOs or parameterized entity constructors
- **Contributing to 39.7% Duplication**: Each mapping ~5 lines duplicated 4 times = 20 lines

---

#### Issue 20: Duplicate View Navigation in Controllers  
**Pattern Seen Across All Controllers**:

```java
// Pattern 1: Success Response (Repeated ~20 times)
ModelAndView mv= new ModelAndView();
mv.setViewName("successPage");
return mv;

// Pattern 2: Failure Response (Repeated ~20 times)
ModelAndView mv= new ModelAndView();
mv.setViewName("failure");
mv.addObject("error",e);
return mv;

// Pattern 3: With Prescription Count (Repeated ~15 times)
ModelAndView mv= new ModelAndView();
mv.setViewName("[viewName]");
mv.addObject("prescriptionsCount", dao1.prescriptionPrintCount());
return mv;
```

**Duplicate Code Analysis**:
- **40+ Near-Identical ModelAndView Configurations**
- **Should Extract**: ViewResponseBuilder or ResponseFactory
- **Reduces Lines by 50%**: Could become `createSuccessView()`, `createFailureView(error)`
- **Contributing to 39.7% Duplication**: Each view config is 3-4 lines, 40 times = 120-160 lines

---

---

## 5. COUPLERS - Code Smell Analysis

### 5.1 FEATURE ENVY

#### Issue 21: LoginController - FEATURE ENVY (Multiple DAO Dependencies)  
**File**: [src/main/java/com/project/controller/LoginController.java](src/main/java/com/project/controller/LoginController.java#L17-27)  
**Lines**: 17-27 (dependency injection section)  

```java
@Controller
public class LoginController {
    @Autowired
    LoginDao dao;                          // Line 20
    
    @Autowired
    PatientPrescriptionDao dao1;           // Line 23 - FEATURE ENVY!
    
    @Autowired
    UsersInSystemDao dao2;                 // Line 26 - FEATURE ENVY!
}
```

**Usage in validate() method (Lines 51-71)**:
```java
public ModelAndView validate(...) {
    // ... login validation ...
    
    if (!userId.equals(null)) {
        HttpSession session= request.getSession();
        Login l=new Login(userId,l1.getRole(),l1.getUsername(),null);
        session.setAttribute("userInfo", l);
        dao.logActivities(session.getId());
        for(Integer i: dao2.getUsersInSystem()) {  // Line 67 - USES dao2
            dao.logActivities(i.toString());
        }
        
        ModelAndView mv= new ModelAndView();
        mv.setViewName("welcome");
        mv.addObject("prescriptionsCount", dao1.prescriptionPrintCount());  // Line 73 - USES dao1
        mv.addObject("users_count", dao2.getUsersInSystem());               // Line 74 - USES dao2
        return mv;
    }
}
```

**Why it's a Smell**:
- **3 Dependencies for Single Operation**: LoginController depends on:
  1. LoginDao - for actual login
  2. PatientPrescriptionDao - for prescription count (PATIENT business logic)
  3. UsersInSystemDao - for user count (ADMIN business logic)
- **Feature Envy**: Controllers asks other objects for data instead of delegating
- **SRP Violation**: Controller shouldn't care about prescription counts or user counts
- **Tight Coupling**: Changes to PatientPrescriptionDao signature break LoginController
- **Testability**: Requires 3 DAO mocks instead of 1

**Refactoring Impact**:
- Current: LoginController → 3 DAOs → Database
- Better: LoginController → LoginService → 3 DAOs → Database
- Or: LoginController → AuthenticationFacade (handles all concerns)

---

#### Issue 22: EditPatientController - EXTREME FEATURE ENVY  
**File**: [src/main/java/com/project/controller/receptionist/EditPatientController.java](src/main/java/com/project/controller/receptionist/EditPatientController.java#L20-32)  
**Dependencies**: 6 different DAOs  

```java
@Controller 
public class EditPatientController {
    @Autowired
    PatientDetailsDao dao1;        // Lines 20-21
    
    @Autowired
    SearchPatientDao dao3;         // Lines 22-23
    
    @Autowired
    EditPatientDao dao2;           // Lines 24-25
    
    @Autowired
    AddPatientDao dao4;            // Lines 26-27
    
    @Autowired
    PatientPrescriptionDao dao5;   // Lines 28-29
    
    @Autowired
    LoginDao infoLog;              // Lines 30-31
}
```

**Usage Pattern**:
```java
// edit() method calls:
Patient temp= dao1.show(pid);                      // From PatientDetailsDao
String doctorname=dao3.searchDoctorAssigned(...);  // From SearchPatientDao
List<String[]> doctors= dao4.getDoctors();         // From AddPatientDao
dao5.prescriptionPrintCount();                     // From PatientPrescriptionDao
```

**Why it's a Smell**:
- **6 Dependencies** for a single resource (Patient)
- **Information Scattered**: Patient logic split across 4 DAOs
- **Controller as Orchestrator**: Lines 35-60 coordinate 4 DAO calls
- **High Coupling**: Each method change in any DAO breaks controller
- **Poor Cohesion**: DAO responsibilities fragmented
- **Testability Nightmare**: Each test requires mocking 6 objects
- **Violates Facade Pattern**: Should have single PatientFacade

**Refactoring**:
```java
// BEFORE: 6 dependencies
EditPatientController → {dao1, dao2, dao3, dao4, dao5, infoLog}

// AFTER: 1 dependency
EditPatientController → PatientFacade → {PatientService, DoctorService, PrescriptionService}
```

---

#### Issue 23: AddEmployeeController - FEATURE ENVY  
**File**: [src/main/java/com/project/controller/administrator/AddEmployeeController.java](src/main/java/com/project/controller/administrator/AddEmployeeController.java#L24-29)  

```java
@Controller
public class AddEmployeeController {
    @Autowired
    AddEmployeeDao dao;        // Knows too much about employee addition
    
    @Autowired
    LoginDao infoLog;          // Knows about logging
    
    // Usage Pattern:
    // - Line 56: Creates Name object (should be in DTO)
    // - Line 57: Creates Address object (should be in DTO)
    // - Line 60: Creates Employee object (should be in DTO/Mapper)
    // - Line 63: Calls dao.add() passing Employee
}
```

**Why it's a Smell**:
- **3 Dependencies** for single operation (add employee)
- **Logic Distribution**: Employee creation scattered (controller + DAO)
- **Knows Business Rules**: Lines 56-60 show controller building domain objects
- **Violates Facade**: Should delegate to EmployeeService, not DAO

---

#### Issue 24: Logging Dependency Everywhere - FEATURE ENVY  
**Every Controller and DAO Injects**:
```java
@Autowired
LoginDao infoLog;  // OR specific logging injections
```

**Usage Pattern (Repeated 100+ times)**:
```java
infoLog.logActivities("in [Class]-[method]: " + data);
```

**Why it's a Smell**:
- **Tight Coupling to LoginDao**: Used just for logging
- **Should Use**: Proper Logger injection (SLF4J, Log4j)
- **Wrong Abstraction**: Logging shouldn't be in LoginDao
- **Contributing to Coupling**: 100+ implicit dependencies on LoginDao.logActivities()
- **Feature Envy**: Every class asks LoginDao to log instead of having own logger

---

### 5.2 MESSAGE CHAINS (Train Wrecks)

#### Issue 25: AddPatientController - MESSAGE CHAIN  
**File**: [src/main/java/com/project/controller/receptionist/AddPatientController.java](src/main/java/com/project/controller/receptionist/AddPatientController.java#L32-50)  

```java
// Line 32-50
List<String[]> doctors= dao.getDoctors();

infoLog.logActivities("in AddPatientController-view:got= ");
for(String[] str: doctors) {
    infoLog.logActivities(str[0]+", "+str[1]+", "+str[2]+", "+str[3]+", ");
}

if(! doctors.equals(null)) {
    ModelAndView mv= new ModelAndView();
    mv.setViewName("receptionist/AddPatientView");
    mv.addObject("prescriptionsCount", dao1.prescriptionPrintCount());
    mv.addObject("doctorsList", doctors);
    return mv;
}
```

**Train Wreck Pattern**:
- Line 37: `dao.getDoctors()` returns `List<String[]>`
- Line 38-40: **Message chain** - iterates String arrays
- Line 42: **Message chain** - calls `doctors.equals(null)`
- Line 46: **Message chain** - calls `dao1.prescriptionPrintCount()`
- Line 47: **Message chain** - calls `mv.setViewName()`, `mv.addObject()`, etc.

**Why it's a Smell**:
- **Tight Coupling**: Controller knows:
  - Doctors returned as String[] (fragile format)
  - String[0] = eid, [1] = firstName, [2] = middleName, [3] = lastName (magic indices!)
  - prescriptionPrintCount() is available on dao1
  - ModelAndView setViewName() and addObject() methods
- **Multiple Levels of Delegation**: Should use DTOs for type-safety
- **Hidden Dependencies**: String array indices are implicit contract
- **Change Impact**: If doctor data structure changes, multiple places break

---

#### Issue 26: EditPatientController - MESSAGE CHAIN  
**File**: [src/main/java/com/project/controller/receptionist/EditPatientController.java](src/main/java/com/project/controller/receptionist/EditPatientController.java#L35-51)  

```java
Patient temp= dao1.show(pid);                                    // Returns Patient
infoLog.logActivities("returned to EditPatientController-edit: got= "+temp);

String doctorname=dao3.searchDoctorAssigned(temp.getDoctorId());
infoLog.logActivities("returned to EditPatientController-edit: got= "+doctorname);

if(!(temp.getPid().equals(null)) && !(doctorname.equals(null))) {
    ModelAndView mv= new ModelAndView();
    mv.setViewName("receptionist/EditPatientView");
    mv.addObject("patient",temp);
    mv.addObject("doctor",doctorname);
    mv.addObject("doctorsList", dao4.getDoctors());
    mv.addObject("prescriptionsCount", dao5.prescriptionPrintCount());
    return mv;
}
```

**Message Chains Identified**:
- Line 35: `dao1.show(pid)` → assumes Patient with getDoctorId()
- Line 37: `temp.getDoctorId()` → message chain #1
- Line 38: `dao3.searchDoctorAssigned(temp.getDoctorId())` → message chain #2
- Line 40: `temp.getPid().equals(null)` → message chain #3
- Line 45: `dao4.getDoctors()` → message chain #4
- Line 46: `dao5.prescriptionPrintCount()` → message chain #5

**Why it's a Smell**:
- **Law of Demeter Violation**: Asks temp for doctorId (1st level), then uses that to ask dao3 (2nd level)
- **Tight Coupling**: Changes to Patient structure break this
- **Fragile Code**: If getDoctorId() returns null, next line crashes
- **Multiple Levels**: 5+ message chains in single method
- **Should Delegate**: Service layer should handle this orchestration

---

---

## 6. SUMMARY TABLE OF CODE SMELLS

| # | Smell Type | File | Lines | Severity | Impact | 
|---|-----------|------|-------|----------|--------|
| 1 | Long Method | AddOpdController | 26-75 | HIGH | 50-line method with switch logic |
| 2 | Long Method | EditPatientController | 62-105 | HIGH | 17 parameters, mixed concerns |
| 3 | Long Method | EditEmployeeController | 72-104 | HIGH | 16 parameters, duplicated logic |
| 4 | Long Method | EditPatientDao | 24-55 | HIGH | Hardcoded queries, 15 params |
| 5 | Long Method | EditEmployeeDao | 25-57 | HIGH | Duplicate DAO, copy-paste error |
| 6 | Long Method | AddPatientDao | 56-94 | HIGH | Mixed ID generation + save logic |
| 7 | Large Class | LoginController | 17-92 | CRITICAL | 6 responsibilities, 3 DAO deps |
| 8 | Large Class | EditPatientController | Full | CRITICAL | 6 DAO dependencies, orchestrator |
| 9 | Duplicate Code | SearchPatientDao | 19-85 | HIGH | 4 methods, same pattern |
| 10 | Duplicate Code | SearchEmployeeDao | Full | HIGH | Copy-paste of SearchPatientDao |
| 11 | Switch Statement | AddOpdController | 35-60 | HIGH | Magic numbers, OO abuse |
| 12 | Refused Bequest | Patient/Employee | Full | MEDIUM | 9 common fields, missing base class |
| 13 | Refused Bequest | Add Controllers | Full | MEDIUM | Should share abstract controller |
| 14 | Divergent Change | Edit DAOs | Both | CRITICAL | Future changes require 2+ edits |
| 15 | Divergent Change | Add DAOs | Both | HIGH | ID generation logic duplicated |
| 16 | Divergent Change | Search DAOs | Both | HIGH | 8 methods, all requiring updates |
| 17 | Duplicate Code | Try-Catch Pattern | Global | CRITICAL | 50+ repetitions across codebase |
| 18 | Duplicate Code | Parameter Mapping | 4 Controllers | HIGH | Same Name/Address creation |
| 19 | Duplicate Code | View Navigation | 40+ places | MEDIUM | ModelAndView configurations |
| 20 | Feature Envy | LoginController | 20-27 | HIGH | 3 DAO dependencies |
| 21 | Feature Envy | EditPatientController | 20-32 | CRITICAL | 6 DAO dependencies |
| 22 | Feature Envy | AddEmployeeController | 24-29 | MEDIUM | 3 dependencies for 1 operation |
| 23 | Feature Envy | Logging in All | Global | HIGH | 100+ LoginDao logging deps |
| 24 | Message Chain | AddPatientController | 32-50 | HIGH | String[] fragility, indices |
| 25 | Message Chain | EditPatientController | 35-51 | HIGH | 5+ chained calls in method |

---

## 7. ROOT CAUSES ANALYSIS

### 7.1 Architectural Issues
1. **No Service Layer**: Controllers call DAOs directly → Feature Envy
2. **No DTO/VO Pattern**: Passing 17+ parameters as strings → Long Methods
3. **No Exception Strategy**: Generic catch blocks → Duplicate Error Handling
4. **No Repository Abstraction**: Direct Hibernate queries everywhere
5. **No Factory Pattern**: Object creation scattered across controllers

### 7.2 Design Issues
1. **No Base Classes**: Identical entities (Patient/Employee) not unified
2. **No Abstract Controllers**: Identical controller logic duplicated
3. **String Array Returns**: `List<String[]>` fragile format
4. **Magic Return Codes**: AddOpdDao returns 1/2/3 with implicit meanings
5. **No Logging Framework**: LoginDao misused for logging

### 7.3 Coding Practices Issues
1. **Copy-Paste Development**: SearchPatientDao/SearchEmployeeDao 90% identical
2. **No Code Review**: Copy-paste errors (e.g., EditEmployeeDao line 52 still says "logindao-validate")
3. **No Unit Testing**: 17-parameter methods untestable
4. **No Refactoring**: CRUD patterns repeated 10+ times
5. **No Documentation**: Magic indices, implicit contracts

---

## 8. REFACTORING RECOMMENDATIONS

### Priority 1 (CRITICAL - Do First)
1. **Create Service Layer**: Add PatientService, EmployeeService, LoginService
2. **Extract DTO Classes**: PatientDTO with 15 fields instead of 17 parameters
3. **Create Base Entity**: PersonEntity with common fields (name, birthdate, etc.)
4. **Replace Magic Numbers**: Use enums for OPD status (PENDING=1, etc.)
5. **Centralize Logging**: Use SLF4J instead of LoginDao.logActivities()

### Priority 2 (HIGH - Do Next)
1. **Create Search Generic**: `<T> GenericSearch<T>.findByField(field, value)`
2. **Extract Edit Logic**: AbstractEditService handling all entity edits
3. **Create Result Objects**: Instead of int/boolean returns, use Result<T> pattern
4. **Implement Factory**: PersonFactory, EntityFactory for object creation
5. **Remove Feature Envy**: Create Facades for multi-DAO operations

### Priority 3 (MEDIUM - Gradually)
1. **Query Builder Pattern**: Replace hardcoded HQL
2. **Spring Data JPA**: Replace manual Hibernate session management
3. **Unit Test Coverage**: Add tests to prevent regressions
4. **API Versioning**: Prepare for changes to entity structures

---

## 9. CODE DUPLICATION HOTSPOTS (39.7% Total)

| Location | Type | Lines | Impact |
|----------|------|-------|--------|
| Search Methods | Try-Catch Template | 60+ | 4 methods × 15 lines each |
| Edit DAOs | Query Building | 40+ | EditPatient + EditEmployee |
| Add DAOs | ID Generation | 30+ | AddPatient + AddEmployee |
| Controllers | Exception Handling | 120+ | 20+ try-catch blocks |
| Entity Classes | Getters/Setters | 80+ | Patient + Employee duplication |
| View Navigation | ModelAndView Config | 100+ | 40+ identical patterns |
| **TOTAL DUPLICATE CODE** | **~430-450 lines** | **~39.7%** | **Refactorable to 40% of current** |

---

## 10. SINGLE RESPONSIBILITY PRINCIPLE VIOLATIONS SUMMARY

**Most Severe Violations**:

1. **AddPatientDao.add()**: Saves patient AND increments ID AND logs (3 responsibilities)
2. **LoginController.validate()**: Validates login AND sets session AND builds dashboard AND tracks users (4 responsibilities)
3. **EditPatientController**: Fetches patient AND searches doctor AND lists doctors AND displays UI (4 responsibilities)
4. **SearchPatientDao**: Executes query AND manages session AND logs AND handles exceptions (4 responsibilities)
5. **Entity Classes**: Store data AND provide getters/setters AND provide toString AND handle multiple concerns

---

**End of Report**
