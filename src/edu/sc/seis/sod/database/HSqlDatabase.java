package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfEvent.*;

import java.util.Properties;
import java.sql.*;
import java.util.*;

import org.hsqldb.*;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
/**
 * HSqlDatabase.java
 *
 *
 * Created: Wed Sep 18 12:18:29 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlDatabase extends AbstractDatabase{
    public HSqlDatabase (){
	
    }

    public HSqlDatabase (Connection connection) {
	super(connection);
	this.configDatabase = new HSqlConfigDatabase(connection, "eventtimeconfig");
    }

    public void create() {
	try {
	    Statement statement = connection.createStatement();
	    try {
		statement.executeUpdate("CREATE TABLE eventconfig "+
					"(eventid int IDENTITY PRIMARY KEY, "+
					" serverName VARCHAR, "+
					" serverDNS VARCHAR, "+
					" eventName VARCHAR, "+
					" latitude float, "+
					" longitude float, "+
					" depth float, "+
					" origin_time timestamp, "+
					" status int, "+
					" eventAccess VARCHAR)");
			} catch(SQLException sqle) {
				
				System.out.println("Table eventconfig is already created");
			}
			
			
		} catch(Exception e) {
			
			e.printStackTrace();
		}
	}
    public String getTableName() {
		return "eventconfig";	
	}

    public ConfigDatabase getConfigDatabase() {
	return this.configDatabase;
    }
    
    private ConfigDatabase configDatabase;    
    
}// HSqlDatabase
