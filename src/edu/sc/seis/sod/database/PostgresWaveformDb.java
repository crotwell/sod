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
			       " numsites int, "+
			       " qtime timestamp) ");

	    stmt.executeUpdate(" CREATE TABLE waveformsitedb "+
			       " ( waveformeventid int, "+
			       " waveformsiteid int, "+
			       " numchannels int, "+
			       " qtime timestamp) ");

	    stmt.executeUpdate(" CREATE TABLE waveformchanneldb "+
			       " ( waveformid int PRIMARY KEY DEFAULT nextval('waveformconfigsequence'),"+
			       " waveformeventid int, "+
			       " waveformchannelid int, "+
			       " qtime timestamp , "+
			       " status int, "+
			       " numretrys int, "+
			       " reason VARCHAR)");
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    
}// PostgresWaveformDb
