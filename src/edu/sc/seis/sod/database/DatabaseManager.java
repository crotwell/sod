package edu.sc.seis.sod.database;

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
    private DatabaseManager() {

    }
    
    public static AbstractDatabaseManager getDatabaseManager(Properties props, String type) {
	if(databaseManager == null) {
	    if(!type.equals("postgres")) {
		databaseManager = new HSqlDbManager(props);
	    } else {
		databaseManager = new PostgresDbManager(props);
	    }
	}
	return databaseManager;
    }
    
    private static AbstractDatabaseManager databaseManager;
    
}// DatabaseManager
