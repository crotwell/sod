package edu.sc.seis.sod.database;

import edu.iris.Fissures.*;

import java.sql.*;

public class PostgresDatabase extends AbstractDatabase {
	
	public PostgresDatabase() {
		
	}
	
	public void create() {
		try {
			String driverName = "org.postgresql.Driver";
			Class.forName(driverName).newInstance();
			
			connection = DriverManager.getConnection("jdbc:postgresql:telukutl",
															"telukutl",	"");
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
	
	public Connection getConnection() {
		return this.connection;		
	}
	
	public String getTableName() {
		return "eventConfig";	
	}
	
	private Connection connection;
	
}
