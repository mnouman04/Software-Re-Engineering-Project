# Quick Reference: Copy-Paste Ready Section for Your Report

---

## B3. Smell Interaction and Prioritisation

### 5. Smell Interaction: Duplicate Code → Shotgun Surgery

The Hospital Management System demonstrates how **Duplicate Code directly causes Shotgun Surgery**. The evidence is found in four DAO/ID generator files containing nearly identical database connection and ID increment logic:

- **PatientIdGenerator.java** (lines 16–28): SQL connection + ResultSet + ID fetch/increment
- **EmployeeIdGenerator.java** (lines 16–28): Identical code with different prefix
- **AddPatientDao.java** (lines 74–88): Query-based ID increment for patients
- **AddEmployeeDao.java** (lines 56–68): Query-based ID increment for employees

**The Causal Chain:**
These four files address a single logical concern—"increment the ID counter"—yet this logic exists in four separate locations. When a database change occurs (e.g., table rename, credential update, connection pooling), a single logical modification must cascade across all four files. For example, if the table `hospital.IdGenerate` must be renamed to `hospital.ID_Sequence`, the following edits are mandatory:

```
Change 1: PatientIdGenerator.java:    "select pid from hospital.IdGenerate"
Change 2: EmployeeIdGenerator.java:   "select eid from hospital.IdGenerate"
Change 3: AddPatientDao.java:         " from IdGenerate"
Change 4: AddEmployeeDao.java:        " from IdGenerate"
```

A developer who misses even one location introduces a production bug: employee creation fails while patient creation succeeds (or vice versa), creating a difficult-to-trace inconsistency. This scenario—where a single business change forces edits to multiple seemingly-unrelated files—is the definition of **Shotgun Surgery**. The 39.7% code duplication in this project directly enables this smell to exist.

---

### 6. Greatest Risk: Duplicate Code as the Primary Maintainability Threat

Among all 235 code smells identified (Bloaters, Change Preventors, Couplers, Dispensables, OO-Abusers), **Duplicate Code represents the single greatest risk to long-term maintainability**. While other smells harm code readability or structure, duplication undermines consistency—the foundation of sustainable development.

**The Numbers:**
- 39.7% of the system's 7,537 lines are duplicated (~3,000 lines)
- ID generation logic alone is replicated 4 times
- Every addition to the system increases duplication risk (developers copy-paste working patterns)

**The Nightmare Scenario:**
A junior developer joins the team and is tasked with adding a new `DoctorHistoryIdGenerator` following the existing pattern. She copies `PatientIdGenerator.java` and modifies the prefix. Six months later, a security audit mandates encryption of all database credentials. The developer who added encryption to `PatientIdGenerator.java` tested it thoroughly and committed. Tests pass. Six weeks into production, the system intermittently fails when creating new doctor records—but patient and employee creation work fine. A full incident review reveals that `DoctorHistoryIdGenerator.java` still uses the old, unencrypted credentials. The junior developer didn't know the code existed elsewhere. The incident costs four hours of downtime and exposes 500 user accounts.

**Why Duplication Outweighs Other Smells:**
- **Long Methods** (50–100 lines): Localized to one function; refactoring has clear boundaries
- **Long Parameter Lists** (17+ params): Affect specific method signatures; fixable via DTOs
- **Divergent Change** (5 files): A service class can consolidate them
- **Duplicate Code** (39.7% system-wide): Permeates the entire codebase; no single refactoring fully eliminates it; the risk compounds silently with each commit

---

### 7. Prioritized Refactoring: Extract Method / Centralize DB Logic (FIRST)

**Proposed Treatment**: Consolidate ID generation and database connection logic into a centralized `DatabaseUtilityService` class, replacing 4 scattered implementations with 1 shared utility.

#### Effort: MINIMAL

- **Scope**: Create one new Java class (~65 lines)
- **Time**: 2–4 hours of focused development
- **Complexity**: Straightforward mechanical extraction; no complex business logic
- **Risk**: Extremely low; refactored code is functionally identical to the original

**The Utility Class (DatabaseUtilityService.java):**
```java
@Component
public class DatabaseUtilityService {
    @Autowired private SessionFactory sf;
    @Autowired private LoginDao infoLog;
    
    @Transactional
    public int generateAndIncrementId(String idField) {
        Session session = sf.getCurrentSession();
        Query q1 = session.createQuery(" from IdGenerate");
        IdGenerate idRecord = (IdGenerate) q1.uniqueResult();
        
        int currentId = "pid".equalsIgnoreCase(idField) 
            ? idRecord.getPid() 
            : idRecord.getEid();
        
        currentId++;
        
        Query q2 = session.createQuery("update IdGenerate set " + idField + "= :newValue");
        q2.setParameter("newValue", currentId);
        q2.executeUpdate();
        
        return currentId;
    }
    
    @Transactional
    public String generateFormattedId(String idField, String prefix) {
        int incrementedId = generateAndIncrementId(idField);
        return prefix + (incrementedId + 101);
    }
}
```

**Refactored ID Generators (Before vs. After):**
| Before | After |
| :----- | :----- |
| 28 lines: Connection, Statement, ResultSet, exception handling | 1 line: `return dbUtility.generateFormattedId("pid", "P");` |

#### Benefit: TRANSFORMATIVE

1. **Eliminates Duplicate Code**: Removes ~50 duplicate lines immediately, representing a 1.6% reduction in system-wide duplication
2. **Eliminates Shotgun Surgery**: The 4-file Shotgun Surgery smell vanishes. Database changes touch exactly 1 implementation
3. **Blocks Future Duplication**: New developers no longer face temptation to copy-paste ID logic
4. **Foundation for Larger Refactors**: Once database logic is unified, the team can tackle Divergent Change (5 files) and Parallel Hierarchies (6 file pairs) with confidence
5. **Measurable Improvements**:
   - Testing burden for ID changes: 4× → 1× (75% reduction)
   - Database change impact: 4 files → 1 file (75% reduction)
   - Inconsistency risk: HIGH → NONE

#### Why This Is First (Not Later)

- **Immediate ROI**: Delivers measurable improvement within days (not weeks)
- **Unblocks Parallel Work**: Multiple developers can safely work on different DAOs
- **Builds Momentum**: Small, successful refactorings inspire confidence for larger changes
- **Lowest Risk**: Implementation is mechanical; no architectural debates; quick rollback if needed

#### Impact Summary

| Metric | Before | After | Change |
| :----- | :----: | :----: | :---- |
| Duplicate ID logic lines | 48 | 1 | ▼ 97.9% |
| Locations for DB changes | 4 | 1 | ▼ 75% |
| Testing effort (ID-related) | 4× | 1× | ▼ 75% |
| Shotgun Surgery files | 4 | 1 | ▼ 75% |
| Time to implement | — | 2–4 hours | ✓ |

---

## Conclusion

The Hospital Management System exhibits a clear causal relationship between **Duplicate Code and Shotgun Surgery**, with duplication representing the highest risk to long-term maintainability. Centralizing database logic is the optimal first refactoring: minimal effort, transformative benefit, and a foundation for sustainable future improvements.

