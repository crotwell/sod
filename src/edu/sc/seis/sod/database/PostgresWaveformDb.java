package edu.sc.seis.sod.database;

import java.sql.*;

import org.apache.log4j.*;

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
	    
	    stmt.executeUpdate(" CREATE TABLE waveformdb "+
			       " ( waveformeventid int, "+
			       " numnetworks int, "+
			       " CONSTRAINT nfkey FOREIGN KEY(waveformeventid) REFERENCES eventconfig(eventid)) ");
	    
	    stmt.executeUpdate(" CREATE TABLE waveformnetworkdb "+
			       " ( waveformeventid int, "+
			       " waveformnetworkid int, "+
			       " numstations int, "+
			       " qtime timestamp) ");
	    stmt.executeUpdate(" CREATE TABLE waveformstationdb "+
			       " ( waveformeventid int, "+
			       " waveformstationid int, "+
			       " waveformnetworkid int, "+
			       " numsites int, "+
			       " qtime timestamp) ");

	    stmt.executeUpdate(" CREATE TABLE waveformsitedb "+
			       " ( waveformeventid int, "+
			       " waveformsiteid int, "+
			       " waveformstationid int , "+
			       " numchannels int, "+
			       " qtime timestamp) ");

	    stmt.executeUpdate(" CREATE TABLE waveformchanneldb "+
			       " ( waveformid int PRIMARY KEY DEFAULT nextval('waveformconfigsequence'),"+
			       " waveformeventid int, "+
			       " waveformchannelid int, "+
			       " waveformsiteid int, "+
			       " qtime timestamp , "+
			       " status int, "+
			       " numretrys int, "+
			       " reason VARCHAR)");
	} catch(SQLException sqle) {
	    logger.debug("one or more tables of the waveform database are already created");
	}
	try {
	    beginTransStmt = connection.prepareStatement("BEGIN TRANSACTION");
	      
	     endTransStmt = connection.prepareStatement("END TRANSACTION");
	     
	     serializableLevelStmt = connection.prepareStatement("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
    	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void beginTransaction() {
	try {
	    System.out.println("BEGINNING TRANSACTION IN POSTGRES ................................................");
	    // System.exit(0);
	    beginTransStmt.executeUpdate();
	    // serializableLevelStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    
    public void endTransaction() {
	try {
	    endTransStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    
    PreparedStatement beginTransStmt;

    PreparedStatement endTransStmt;

    PreparedStatement serializableLevelStmt;

    static Category logger = 
        Category.getInstance(PostgresWaveformDb.class.getName());
    
}// PostgresWaveformDb
