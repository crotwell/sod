package edu.sc.seis.sod.database;

import edu.iris.Fissures.*;

import java.util.Properties;
import java.sql.*;

public class PostgresDatabase extends AbstractDatabase {
    
    public PostgresDatabase(Connection connection) {
	super(connection);
	configDatabase = new PostgresConfigDatabase(connection);
    }
    
	
	public void create() {
		try {
		    
			Statement stmt = connection.createStatement();
			
			try {
				stmt.executeUpdate("CREATE SEQUENCE eventconfigsequence");
				stmt.executeUpdate(" CREATE TABLE "+getTableName()+
								   " (eventid int primary key DEFAULT nextval('eventconfigsequence'), "+
								   " serverName text, "+
								   " serverDNS text, "+
								   " eventName text, "+
								   " latitude float, "+
								   " longitude float, "+
								   " depth float, "+
								   " origin_time timestamp, "+
								   " status int, "+
								   " eventAccess text)");
				
			} catch(SQLException sqle) {
				sqle.printStackTrace();
				System.out.println("The table "+getTableName()+" is already created ");
			}
			} catch(Exception e) {
			e.printStackTrace();		
		}
		
	}	
    
	public String getTableName() {
		return "eventConfig";	
	}

	public ConfigDatabase getConfigDatabase() {
		return this.configDatabase;
	}
	

	private ConfigDatabase configDatabase;
	
}
