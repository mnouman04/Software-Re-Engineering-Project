package com.project.utility;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.project.dao.LoginDao;
import com.project.entity.IdGenerate;

/**
 * CENTRALIZED DATABASE UTILITY SERVICE
 * 
 * Purpose: Consolidate all duplicate ID generation and increment logic into a single,
 *          reusable service. This eliminates the Shotgun Surgery smell by providing
 *          one source of truth for ID management across all DAOs.
 * 
 * Benefit: Future database changes (table name, column names, ID generation strategy)
 *          only require modification in this single class, not in 4+ scattered locations.
 */
@Component
public class DatabaseUtilityService 
{
    @Autowired
    private SessionFactory sf;
    
    @Autowired
    private LoginDao infoLog;
    
    /**
     * Centralized method to generate and increment ID values.
     * 
     * @param idField The name of the ID field to increment ("pid", "eid", etc.)
     * @return The incremented ID value
     */
    @Transactional
    public int generateAndIncrementId(String idField) throws HibernateException 
    {
        try {
            Session session = sf.getCurrentSession();
            
            // Fetch current ID record
            Query q1 = session.createQuery(" from IdGenerate");
            IdGenerate idRecord = (IdGenerate) q1.uniqueResult();
            
            if (idRecord == null) {
                throw new RuntimeException("IdGenerate table is empty. Cannot generate ID.");
            }
            
            // Get current value based on field type
            int currentId = 0;
            if ("pid".equalsIgnoreCase(idField)) {
                currentId = idRecord.getPid();
                infoLog.logActivities("Incrementing PID from: " + currentId);
            } 
            else if ("eid".equalsIgnoreCase(idField)) {
                currentId = idRecord.getEid();
                infoLog.logActivities("Incrementing EID from: " + currentId);
            }
            else {
                throw new IllegalArgumentException("Unknown ID field: " + idField);
            }
            
            // Increment the ID
            currentId++;
            
            // Update the IdGenerate table
            String updateQuery = "update IdGenerate set " + idField + "= :newValue";
            Query q2 = session.createQuery(updateQuery);
            q2.setParameter("newValue", currentId);
            int updateStatus = q2.executeUpdate();
            
            infoLog.logActivities("ID increment successful. New " + idField + " = " + currentId + 
                                ". Update status = " + updateStatus);
            
            return currentId;
        } 
        catch (Exception e) {
            infoLog.logActivities("ERROR in DatabaseUtilityService-generateAndIncrementId: " + e.getMessage());
            throw new HibernateException("Failed to generate and increment ID", e);
        }
    }
    
    /**
     * Alternative method: Generate string-based ID with custom prefix.
     * Useful for PatientIdGenerator, EmployeeIdGenerator, etc.
     * 
     * @param idField The numeric ID field name ("pid", "eid", etc.)
     * @param prefix The prefix to add (e.g., "P", "EMP", "DOC")
     * @return The formatted ID string (e.g., "P102", "EMP105")
     */
    @Transactional
    public String generateFormattedId(String idField, String prefix) throws HibernateException 
    {
        int incrementedId = generateAndIncrementId(idField);
        int numericId = incrementedId + 101;  // Original offset logic preserved
        String formattedId = prefix + numericId;
        infoLog.logActivities("Generated formatted ID: " + formattedId);
        return formattedId;
    }
}
