
package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import org.hsqldb.*;


/**
 * HSqlConfigDatabase.java
 *
 *
 * Created: Thu Sep 12 15:33:59 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlConfigDatabase extends AbstractConfigDatabase{
    public HSqlConfigDatabase (Connection connection){
	super(connection);
    }
    
    public  void create() {
	try {
	    Statement stmt = connection.createStatement();
	    try {
		stmt.executeUpdate("CREATE TABLE timeconfig "+
				   "( serverName VARCHAR, "+
				   " serverDNS VARCHAR, "+
				   " time timestamp)");
	    } catch(SQLException sqle) {
		System.out.println("Table timeconfig  is already created");
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	}
	
	
    }
 
}// HSqlConfigDatabase
