package com.project.dao.administrator;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.project.dao.LoginDao;
import com.project.entity.Employee;
import com.project.entity.IdGenerate;
import com.project.entity.Login;
import com.project.utility.DatabaseUtilityService;

/**
 * REFACTORED: AddEmployeeDao
 * 
 * KEY CHANGE: Replaced 10-15 lines of duplicate ID increment logic with a single
 *             call to DatabaseUtilityService.generateAndIncrementId("eid")
 * 
 * Lines removed: ~12 duplicate lines (Query creation, parameter setting, executeUpdate)
 * Lines added:   ~1 method call
 * Net reduction: ~11 duplicate lines eliminated from this single DAO
 * 
 * Shotgun Surgery Impact: BEFORE: Changing ID generation required edits in 4 files
 *                         AFTER:  All changes centralized in DatabaseUtilityService
 */
@Component
public class AddEmployeeDao 
{
    @Autowired
    private SessionFactory sf;
    
    @Autowired
    private LoginDao infoLog;
    
    @Autowired
    private DatabaseUtilityService dbUtility;  // ← NEW: Centralized utility injection
    
    @Transactional
    public boolean add(Employee e)
    {
        try
        {
            Date date = new Date();
            e.setJoiningDate(date);
            e.setStatus(1);
            
            infoLog.logActivities("in AddEmployeeDao-add: got= " + e);
            
            Session session = sf.getCurrentSession();
            session.save(e);
            
            // Storing info in Login table
            String id = e.getEid();
            String role = e.getRole();
            String username = e.getEid();
            
            String password = BCrypt.hashpw(e.getAdharNo() + "", BCrypt.gensalt());
            infoLog.logActivities("aadhar no= " + e.getAdharNo() + ", generated hash= " + password);
            Login l = new Login(id, role, username, password);
            infoLog.logActivities("" + l);
            session.save(l);
            
            // ✓ REFACTORED: Replaced 10-line duplicate block with single utility call
            dbUtility.generateAndIncrementId("eid");
            
            infoLog.logActivities("in AddEmployeeDao-add: Employee saved and ID incremented successfully");
            return true;
        }
        catch (Exception ex)
        {
            infoLog.logActivities("in AddEmployeeDao-add: " + ex);
            return false;
        }
    }
}
