# Change Preventors Audit for Hospital Management System

This audit flags Category 3 change-preventing smells with exact file references and suggested treatments.

| Smell Name | File(s) and Lines | How Many Places Must Change? | Treatment Strategy |
| :--- | :--- | :--- | :--- |
| Shotgun Surgery | `src/main/java/com/project/dao/receptionist/AddPatientDao.java` (lines 74-88), `src/main/java/com/project/dao/administrator/AddEmployeeDao.java` (lines 56-61), `src/main/java/com/project/dao/receptionist/PatientIdGenerator.java` (lines 16-28), `src/main/java/com/project/dao/administrator/EmployeeIdGenerator.java` (lines 16-28) | 4 | Extract shared ID-generation/DB metadata into a common service or repository. Replace hard-coded table/column references with centralized constants or a single `IdGenerateRepository` so a schema change touches one implementation instead of four. |
| Divergent Change | `src/main/java/com/project/controller/receptionist/SearchPatientController.java` (lines 28-181) | 5 | Extract search and validation logic into a dedicated `PatientSearchService` and keep the controller responsible only for request/view mapping. Separate `SearchPatientViewController` from `PatientSearchService` so UI layout changes do not require touching database search behavior in multiple endpoint methods. |
| Parallel Inheritance Hierarchies | `src/main/java/com/project/controller/receptionist/AddPatientController.java` (lines 20-94), `src/main/java/com/project/dao/receptionist/AddPatientDao.java` (lines 17-98), `src/main/java/com/project/controller/administrator/AddEmployeeController.java` (lines 21-76), `src/main/java/com/project/dao/administrator/AddEmployeeDao.java` (lines 15-68), `src/main/java/com/project/controller/doctor/PatientHistoryController.java` (lines 1-86), `src/main/java/com/project/dao/doctor/PatientHistoryDao.java` (lines 1-110) | 6 | Replace parallel controller/DAO class pairs with a role-parameterized service or generic repository layer. Use shared service interfaces like `UserEntityService<T>` and user-type strategies instead of one standalone controller+DAO pair per role. |

## Notes
- The `IdGenerate` table is referenced directly in both add DAOs and two identifier generators, making schema or naming changes especially brittle.
- `SearchPatientController` contains both view selection/UI model preparation and database query validation across multiple endpoints, so unrelated changes are likely to require edits in several blocks.
- The application currently grows user types by pairing a `*Controller` with a matching `*Dao`, which is a classic parallel hierarchy smell. Adding a new user or domain role will likely force the creation of a new controller and DAO together.
