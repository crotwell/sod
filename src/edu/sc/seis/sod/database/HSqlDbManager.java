package edu.sc.seis.sod.database;

import java.sql.*;
import java.util.*;
import org.apache.log4j.*;
/**
 * HSqlDbManager.java
 *
 *
 * Created: Thu Oct  3 14:44:46 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlDbManager extends AbstractDatabaseManager{
    public HSqlDbManager (Properties props){
	super(props);
	//here set the log size and if possible even the size of the script file.
	//setLogSize();
    }

     public ConfigDatabase getConfigDatabase() {
	//return new EventConfigDb();
	if(eventConfigDb == null) {
	    eventConfigDb = new HSqlConfigDatabase(getConnection(), "notused");
	}
	return eventConfigDb;
    }

    public EventDatabase getEventDatabase() {
	if(eventDatabase == null) {
	    eventDatabase = new HSqlDatabase(getConnection());
	}
	return eventDatabase;
	
    }
    
     public NetworkDatabase getNetworkDatabase() {
	 if(networkDatabase == null) {
	     networkDatabase = new HSqlNetworkDb(getConnection());
	}
	return networkDatabase;
    }

    public WaveformDatabase getWaveformDatabase() {
	if(waveformDatabase == null) {
	    waveformDatabase = new HSqlWaveformDb(getConnection());
	}
	return waveformDatabase;
    }

    public Connection getConnection() {
	try {
	    if(connection == null) {
		String driverName = new String("org.hsqldb.jdbcDriver");
		Class.forName(driverName).newInstance();
        System.out.println("The database name is "+getDatabaseName());
		connection = DriverManager.getConnection("jdbc:hsqldb:"+getDatabaseName(), "sa", "");
		
	    } 
	    return connection;
	} catch(Exception sqle) {
	    //	    sqle.printStackTrace();
	    logger.debug("Unable to create the connection to HSQLDB database "+getDatabaseName(),sqle);
	    return null;
	}
	
    }

    private void setLogSize() {
	try {
	     Connection connection = getConnection();
	     if(connection == null) return;
	     Statement stmt = connection.createStatement();
	    stmt.executeUpdate("SET LOGSIZE 10");
	} catch(SQLException sqle) {
	    //error cannot set the logsize.
	    //sqle.printStackTrace();
	    logger.debug("ERROR: unable to set the log size ");
	}
    }

    
    private ConfigDatabase eventConfigDb;
    
    private EventDatabase eventDatabase;
    
    private NetworkDatabase networkDatabase;

    private WaveformDatabase waveformDatabase;
    
    private Connection connection;

    private static Category logger = Category.getInstance(HSqlDbManager.class.getName());
    
}// HSqlDbManager
