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
	stmt.executeUpdate(" CREATE TABLE waveformdatabase "+
			   " ( waveformid int IDENTITY PRIMARY KEY, "+
			   " waveformeventid int, "+
			   "waveformnetworkid int,  "+// FOREIGN KEY REFERENCES networkdatabase(networkid), "+
			   " status int, "+
			   " last_time timestamp, "+
			   " numretrys int, "+
			   " reason VARCHAR, "+
			   " CONSTRAINT nfkey FOREIGN KEY(waveformeventid) REFERENCES eventconfig(eventid)) ");
	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}
    }
    
}// HSqlWaveformDb
