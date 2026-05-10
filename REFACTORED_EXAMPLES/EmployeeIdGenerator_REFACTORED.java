package com.project.dao.administrator;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.dao.LoginDao;
import com.project.utility.DatabaseUtilityService;

/**
 * REFACTORED: EmployeeIdGenerator
 * 
 * BEFORE: 28 lines of connection, statement, and result set handling code
 * AFTER:  5 lines leveraging centralized DatabaseUtilityService
 * 
 * Benefit: Identical to PatientIdGenerator refactoring. Duplicate code eliminated.
 *          Future modifications to ID generation logic apply instantly across all entity types.
 */
public class EmployeeIdGenerator implements IdentifierGenerator
{
    @Autowired
    private DatabaseUtilityService dbUtility;
    
    @Autowired
    private LoginDao infoLog;
    
    public Serializable generate(SharedSessionContractImplementor session, Object object) 
        throws HibernateException 
    {
        // ✓ REFACTORED: One line replaces 25+ lines of duplicate code
        return dbUtility.generateFormattedId("eid", "EMP");
    }
}
