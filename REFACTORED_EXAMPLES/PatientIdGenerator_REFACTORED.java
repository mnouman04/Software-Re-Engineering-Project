package com.project.dao.receptionist;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import com.project.dao.LoginDao;
import com.project.utility.DatabaseUtilityService;

/**
 * REFACTORED: PatientIdGenerator
 * 
 * BEFORE: 28 lines of connection, statement, and result set handling code
 * AFTER:  5 lines leveraging centralized DatabaseUtilityService
 * 
 * Benefit: Eliminates duplicate SQL/connection logic. If database credentials
 *          or table structure changes, only DatabaseUtilityService needs updating.
 */
public class PatientIdGenerator implements IdentifierGenerator
{
    @Autowired
    private DatabaseUtilityService dbUtility;
    
    @Autowired
    private LoginDao infoLog;
    
    public Serializable generate(SharedSessionContractImplementor session, Object object) 
        throws HibernateException 
    {
        // ✓ REFACTORED: One line replaces 25+ lines of duplicate code
        return dbUtility.generateFormattedId("pid", "P");
    }
}
