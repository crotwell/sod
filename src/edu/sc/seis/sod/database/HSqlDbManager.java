package edu.sc.seis.sod.database;

import java.sql.*;
import java.util.*;
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
		connection = DriverManager.getConnection("jdbc:hsqldb:"+getDatabaseName(), "sa", "");
	    } 
	    return connection;
	} catch(Exception sqle) {
	    sqle.printStackTrace();
	    return null;
	}
	
    }

    
    private ConfigDatabase eventConfigDb;
    
    private EventDatabase eventDatabase;
    
    private NetworkDatabase networkDatabase;

    private WaveformDatabase waveformDatabase;
    
    private Connection connection;
    
}// HSqlDbManager
