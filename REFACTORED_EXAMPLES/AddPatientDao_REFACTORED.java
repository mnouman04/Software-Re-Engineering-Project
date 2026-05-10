package com.project.dao.receptionist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.dao.LoginDao;
import com.project.entity.Employee;
import com.project.entity.Patient;
import com.project.utility.DatabaseUtilityService;

/**
 * REFACTORED: AddPatientDao
 * 
 * KEY CHANGE: Replaced 10-15 lines of duplicate ID increment logic with a single
 *             call to DatabaseUtilityService.generateAndIncrementId("pid")
 * 
 * Lines removed: ~12 duplicate lines (Query creation, parameter setting, executeUpdate)
 * Lines added:   ~1 method call
 * Net reduction: ~11 duplicate lines eliminated from this single DAO
 * 
 * Shotgun Surgery Impact: If ID generation logic changes (e.g., add encryption,
 *                         change table name), this file no longer needs modification.
 */
@Component
public class AddPatientDao 
{
    @Autowired
    private SessionFactory sf;
    
    @Autowired
    private LoginDao infoLog;
    
    @Autowired
    private DatabaseUtilityService dbUtility;  // ← NEW: Centralized utility injection
    
    @SuppressWarnings("null")
    @Transactional
    public List<String[]> getDoctors()
    {
        Session session = sf.getCurrentSession();
        Query q1 = session.createQuery(" from Employee where role= :r AND status=:s");
        q1.setParameter("r", "doctor");
        q1.setParameter("s", 1);
        
        try {
            List<Employee> l1 = (List<Employee>) q1.list();
            infoLog.logActivities("in AddPatientDao-getDoctors:found= " + l1);
            
            List<String[]> doctorList = new ArrayList<String[]>();
            
            for (Employee e : l1) {
                String[] temp = new String[4];
                temp[0] = e.getEid();
                temp[1] = e.getName().getFirstName();
                temp[2] = e.getName().getMiddleName();
                temp[3] = e.getName().getLastName();
                doctorList.add(temp);
            }
            infoLog.logActivities("in AddPatientDao-getDoctors:found= " + doctorList);
            
            return doctorList;
        }
        catch (Exception e) {
            infoLog.logActivities("in AddPatientDao-getDoctors: " + e);
            return null;
        }
    }
    
    @Transactional
    public boolean add(Patient p1) 
    {
        infoLog.logActivities("in AddPatientDao-add: got= " + p1);
        
        try {
            Date date = new Date();
            p1.setRegistrationDate(date);
            
            Session session = sf.getCurrentSession();
            session.save(p1);
            
            // ✓ REFACTORED: Replaced 10-line duplicate block with single utility call
            dbUtility.generateAndIncrementId("pid");
            
            infoLog.logActivities("in AddPatientDao-add: Patient saved and ID incremented successfully");
            return true;
        }
        catch (Exception e) {
            infoLog.logActivities("in AddPatientDao-add: " + e);
            return false;
        }
    }
}
