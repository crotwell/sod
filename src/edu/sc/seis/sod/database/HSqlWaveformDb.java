package edu.sc.seis.sod.database;

import java.sql.*;
import org.hsqldb.*;
import org.apache.log4j.*;

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
	    stmt.executeUpdate(" CREATE CACHED TABLE waveformdb "+
			       " ( waveformeventid int, "+
			       " numnetworks int, "+
			       " CONSTRAINT nfkey FOREIGN KEY(waveformeventid) REFERENCES eventconfig(eventid)) ");
	    
	    stmt.executeUpdate(" CREATE CACHED TABLE waveformnetworkdb "+
			       " ( waveformeventid int, "+
			       " waveformnetworkid int, "+
			       " numstations int, "+
			       " qtime timestamp) ");
	    stmt.executeUpdate(" CREATE CACHED TABLE waveformstationdb "+
			       " ( waveformeventid int, "+
			       " waveformstationid int, "+
			       " waveformnetworkid int, "+
			       " numsites int, "+
			       " qtime timestamp) ");

	    stmt.executeUpdate(" CREATE CACHED TABLE waveformsitedb "+
			       " ( waveformeventid int, "+
			       " waveformsiteid int, "+
			       " waveformstationid int, "+
			       " numchannels int, "+
			       " qtime timestamp) ");
	    stmt.executeUpdate(" CREATE CACHED TABLE waveformchanneldb "+
			       " ( waveformid  int IDENTITY PRIMARY KEY,"+
			       " waveformeventid int, "+
			       " waveformchannelid int, "+
			       " waveformsiteid int, "+
			       " qtime timestamp , "+
			       " status int, "+
			       " numretrys int, "+
			       " reason VARCHAR)");

        stmt.executeUpdate(" CREATE INDEX waveformchannelindex ON waveformchanneldb "+
                           " (waveformeventid, waveformchannelid)");

        stmt.executeUpdate(" CREATE INDEX waveformstationindex ON waveformstationdb "+
                           " (waveformeventid, waveformstationid)");
        
        
        stmt.executeUpdate(" CREATE INDEX waveformnetworkindex ON waveformnetworkdb "+
                           " (waveformeventid, waveformnetworkid)");            

        stmt.executeUpdate(" CREATE INDEX waveformsiteindex ON waveformsitedb "+
                           " (waveformeventid, waveformsiteid)");
	  

	    
	} catch(SQLException sqle) {
	    logger.debug("one or more tables of waveformdatabase are already created");
	}
	try {
	     beginTransStmt = connection.prepareStatement("SET AUTOCOMMIT FALSE");
	      
	     endTransStmt = connection.prepareStatement("COMMIT");
    	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void beginTransaction() {
	try {
	    beginTransStmt.executeUpdate();
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

    static Category logger = 
	Category.getInstance(HSqlWaveformDb.class.getName());
    
}// HSqlWaveformDb
