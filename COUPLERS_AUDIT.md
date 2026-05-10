# Couplers Audit for Hospital Management System

This audit flags Category 5 coupler smells with exact file references and suggested treatments.

| Smell Name | File and Line | Description of the Coupling Problem | Treatment |
| :--- | :--- | :--- | :--- |
| Feature Envy | `src/main/java/com/project/controller/receptionist/SearchPatientController.java` (lines 35-45, 75-85) | Controller methods access Patient's doctorId field and then call another DAO method to get doctor name, performing data processing that should belong to Patient or a service | Move Method: Extract doctor name retrieval into a Patient service method or add getDoctorName() to Patient entity |
| Inappropriate Intimacy | `src/main/java/com/project/controller/receptionist/SearchPatientController.java` (lines 35-45) | Controller directly accesses Patient's internal field (getDoctorId()) and knows about DAO structure to fetch related data | Hide Delegate: Introduce a service layer to encapsulate Patient-Doctor relationship queries |
| Message Chain | `src/main/java/com/project/controller/LoginController.java` (line 65) | Code uses request.getSession().getAttribute("userInfo").getRole() creating a chain of method calls that tightly couples to session and Login structure | Hide Delegate: Create a session utility method like getCurrentUserRole() to break the chain |
| Middleman | `src/main/java/com/project/controller/receptionist/AddPatientController.java` (lines 60-85) | Controller acts as a middleman, simply creating domain objects from request params and forwarding to DAO without adding business logic | Replace Delegation with Inheritance or Remove Middleman: Move object creation logic to a service or factory class |
| Incomplete Library Class | `src/main/java/com/project/dao/doctor/PatientHistoryDao.java` (lines 54-56) | Manual date string manipulation using substring on Date.toString() instead of proper formatting, indicating SimpleDateFormat is insufficient | Introduce Local Method: Create a utility method for date formatting or use a complete date library like Joda-Time |

## Notes
- Controllers frequently bypass service layers, directly coupling to DAOs and entity internals.
- Session attribute access creates chains that make testing and refactoring difficult.
- Date handling is inconsistent, with manual formatting where libraries could provide better support.</content>
<parameter name="filePath">d:\RE_Final_Project\HospitalManagement\COUPLERS_AUDIT.md