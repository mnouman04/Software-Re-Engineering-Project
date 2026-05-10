# Part C: Dependency Mapping

## Coupling Metrics Analysis

### Selected Classes
For this architectural analysis, six key classes were selected from the Hospital Management System codebase:

1. **LoginController** - Handles user authentication and login requests
2. **AddEmployeeController** - Manages employee addition operations (assuming this represents AddDoctorController as doctors are employees)
3. **PatientHistoryController** - Provides patient medical history functionality
4. **AddEmployeeDao** - Data access layer for employee operations (assuming this represents AddDoctorDao)
5. **LoginDao** - Data access layer for authentication operations
6. **Employee** - Entity class representing employee data (assuming this represents DoctorEntity as doctors are employees)

### Coupling Calculations

| Class | Ca (Afferent Coupling) | Ce (Efferent Coupling) | Instability (I = Ce/(Ca+Ce)) |
|-------|----------------------|----------------------|-----------------------------|
| LoginController | 0 | 4 | 1.00 |
| AddEmployeeController | 0 | 5 | 1.00 |
| PatientHistoryController | 0 | 4 | 1.00 |
| AddEmployeeDao | 1 | 4 | 0.80 |
| LoginDao | 6 | 1 | 0.14 |
| Employee | 13 | 3 | 0.19 |

### Detailed Working for LoginController and LoginDao

#### LoginController
**Afferent Coupling (Ca) = 0**
- No classes in the codebase import or reference LoginController
- This is typical for controller classes in Spring MVC applications, which are primarily invoked through HTTP requests rather than direct method calls from other classes

**Efferent Coupling (Ce) = 4**
- LoginDao (authentication data access)
- UsersInSystemDao (system user management)
- PatientPrescriptionDao (patient prescription operations)
- Login (entity class for login data)

**Instability (I) = 4/(0+4) = 1.00**
- Maximally unstable class with no incoming dependencies
- Highly dependent on other components, making it sensitive to changes in dependent classes

#### LoginDao
**Afferent Coupling (Ca) = 6**
- LoginController (uses for authentication validation)
- SearchPatientController (uses for logging activities)
- PatientPrescriptionController (uses for logging activities)
- EditPatientController (uses for logging activities)
- AddPatientController (uses for logging activities)
- PersonalInfoController (uses for logging activities)

**Efferent Coupling (Ce) = 1**
- Login (entity class for login data)

**Instability (I) = 1/(6+1) = 0.14**
- Relatively stable class with multiple dependents
- Low instability indicates good design with controlled dependencies

### Architectural Insights

1. **Controller Layer Instability**: All three controller classes (LoginController, AddEmployeeController, PatientHistoryController) exhibit maximum instability (I=1.00), indicating they are highly dependent on other components but have no incoming dependencies from within the application code.

2. **DAO Layer Stability**: The DAO classes show better stability metrics, with LoginDao being particularly stable (I=0.14) due to its central role in authentication logging across multiple controllers.

3. **Entity Stability**: The Employee entity demonstrates good stability (I=0.19) with high afferent coupling (13 classes) and moderate efferent coupling (3 classes), indicating it's a well-utilized core component.

4. **Dependency Patterns**: The analysis reveals a typical layered architecture where controllers depend heavily on DAOs and entities, while DAOs and entities serve multiple consumers, creating a stable foundation layer.

### Recommendations

1. **Reduce Controller Coupling**: Consider introducing service layers to mediate between controllers and DAOs, reducing the high efferent coupling of controller classes.

2. **Maintain DAO Stability**: The current stability of DAO classes is good; avoid introducing unnecessary dependencies that could increase their instability.

3. **Entity Design**: The Employee entity shows balanced coupling; continue monitoring as the system evolves to ensure it doesn't become overly coupled.

This dependency mapping provides a quantitative foundation for understanding the architectural coupling within the Hospital Management System, enabling informed refactoring decisions to improve maintainability and reduce technical debt.</content>
<parameter name="filePath">d:\RE_Final_Project\HospitalManagement\PART_C_DEPENDENCY_MAPPING.md