
package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import org.apache.log4j.*;


/**
 * PostgresConfigDatabase.java
 *
 *
 * Created: Thu Sep 12 15:33:59 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PostgresConfigDatabase extends AbstractConfigDatabase{
    public PostgresConfigDatabase (Connection connection, String tableName){
	super(connection, tableName);
    }
    
    public  void create() {
	try {
	    Statement stmt = connection.createStatement();
	    try {
		stmt.executeUpdate("CREATE TABLE "+tableName+
				   "( serverName text, "+
				   " serverDNS text, "+
				   " time timestamp)");
	    } catch(SQLException sqle) {
		System.out.println("Table timeconfig  is already created");
	    }
	} catch(Exception e) {
	    logger.debug("table "+tableName+" is already created");
	}
    }

    static Category logger = 
        Category.getInstance(PostgresConfigDatabase.class.getName());
}// PostgresConfigDatabase
