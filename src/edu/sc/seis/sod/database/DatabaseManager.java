package edu.sc.seis.sod.database;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
/**
 * DatabaseManager.java
 *
 *
 * Created: Thu Oct  3 14:27:47 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DatabaseManager {
    private DatabaseManager() {}
    
    public static AbstractDatabaseManager getDatabaseManager(Properties props, String type) {
        String className = null;
        try {
            if(databaseManager != null) return databaseManager;
            //use reflection to get the appropriate Database Manager.
            className = getDatabaseType(props);
            Class[] constructorArgTypes = new Class[1];
            constructorArgTypes[0] = Properties.class;
            Class externalClass = Class.forName(className);
            Constructor constructor =
                externalClass.getConstructor(constructorArgTypes);
            Object[] constructorArgs = new Object[1];
            constructorArgs[0] = props;
            Object obj =
                constructor.newInstance(constructorArgs);
            databaseManager = (AbstractDatabaseManager)obj;
            return databaseManager;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String getDatabaseType(Properties props) {
        if(props == null) return "edu.sc.seis.sod.database.HSqlDbManager";
        String rtnValue = props.getProperty("edu.sc.seis.sod.databasetype");
        if(rtnValue != null) return rtnValue;
        return "edu.sc.seis.sod.database.HSqlDbManager";
    }
    
    private static AbstractDatabaseManager databaseManager;
    
}// DatabaseManager
