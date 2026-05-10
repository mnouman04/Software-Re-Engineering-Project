# Dispensables Audit for Hospital Management System

This audit flags Category 4 dispensable smells with exact file references and suggested treatments.

| Smell Name | File and Line | What Makes It Dispensable? | Treatment |
| :--- | :--- | :--- | :--- |
| Duplicate Code | `src/main/java/com/project/dao/receptionist/PatientIdGenerator.java` (lines 20-38), `src/main/java/com/project/dao/administrator/EmployeeIdGenerator.java` (lines 20-38) | Identical SQL connection, statement creation, and ID generation logic duplicated between patient and employee ID generators | Extract Method: Create a shared `BaseIdGenerator` class with `generateId(String prefix, String columnName)` method |
| Comments | `src/main/java/com/project/entity/Patient.java` (line 17), `src/main/java/com/project/entity/Employee.java` (line 21), `src/main/java/com/project/entity/Opd.java` (line 19) | Redundant comments like "//to store date i.e. yyyy-mm-dd" above @Temporal(TemporalType.DATE) which is self-explanatory | Remove Comment: Delete redundant explanatory comments that merely restate the code |
| Comments | `src/main/java/com/project/dao/receptionist/AddPatientDao.java` (lines 93-98) | Commented-out catch block with exception handling code | Remove dead code: Delete commented-out exception handling |
| Data Class | `src/main/java/com/project/entity/Patient.java` (entire class), `src/main/java/com/project/entity/Employee.java` (entire class), `src/main/java/com/project/entity/Address.java` (entire class), `src/main/java/com/project/entity/Name.java` (entire class) | Entity classes contain only private fields, getters/setters, constructors, and toString() with no business logic | Move Method: Extract validation or business logic into service classes if needed, or keep as data holders if appropriate |
| Speculative Generality | `src/main/java/com/project/entity/_OpdRecord.java` (entire class) | Class not annotated with @Entity and comment says "we dont want to store its data in database", suggesting it was created for future use but may be unused | Collapse Hierarchy or Remove: If not used in production, delete the class; if used as DTO, rename to reflect purpose |

## Notes
- Entity classes like Patient and Employee are typical data classes in JPA/Hibernate applications, but if they accumulate business logic, consider refactoring.
- The ID generator duplication affects maintainability when changing ID generation logic.
- Commented-out code should be removed to avoid confusion and reduce code clutter.</content>
<parameter name="filePath">d:\RE_Final_Project\HospitalManagement\DISPENSABLES_AUDIT.md