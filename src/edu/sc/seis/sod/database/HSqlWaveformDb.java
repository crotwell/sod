package edu.sc.seis.sod.database;

import java.sql.*;
import org.hsqldb.*;

/**
 * HSqlWaveformDb.java
 *
 *
 * Created: Fri Oct 11 14:42:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlWaveformDb extends AbstractWaveformDatabase{
    public HSqlWaveformDb (Connection connection){
	super(connection);
    }
    
    public void create() {
	try {
	    Statement stmt = connection.createStatement();
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
			       " ( waveformid  int IDENTITY PRIMARY KEY,"+
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
    
}// HSqlWaveformDb
