package edu.sc.seis.sod.database;

import java.sql.*;

/**
 * PostgresWaveformDb.java
 *
 *
 * Created: Fri Oct 11 14:42:13 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PostgresWaveformDb extends AbstractWaveformDatabase{
    public PostgresWaveformDb (Connection connection){
	super(connection);
    }
    
    public void create() {
	try { 
	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate("CREATE SEQUENCE waveformconfigsequence");
	    stmt.executeUpdate("CREATE table waveformdatabase "+
			       " ( waveformid int PRIMARY KEY DEFAULT nextval('waveformconfigsequence'), "+
			       " waveformeventid int CONSTRAINT eventfkey REFERENCES eventconfig(eventid), "+
			       " waveformnetworkid int, "+
			       //" networkdatabase(networkid), "+
			       " status int , "+
			       " last_time timestamp, "+
			       " numretrys int, "+
			       " reason text )");
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    
}// PostgresWaveformDb
