package edu.sc.seis.sod.database;


import java.sql.*;
import java.util.*;
/**
 * AbstractWaveformDatabase.java
 *
 *
 * Created: Fri Oct 11 14:40:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract class AbstractWaveformDatabase implements WaveformDatabase{
    public AbstractWaveformDatabase (Connection connection){
	this.connection = connection;
	init();
    }

    private void init() {
	try {
		create();
		getStmt = connection.prepareStatement(" SELECT waveformeventid, waveformnetworkid "+
						      " FROM waveformdatabase WHERE waveformid = ? ");
		getIdStmt = connection.prepareStatement(" SELECT waveformid FROM "+
							" waveformdatabase "+
							" WHERE waveformeventid = ?  AND "+
							" waveformnetworkid =? ");
		putStmt = connection.prepareStatement(" INSERT INTO waveformdatabase "+
						      " (waveformeventid, waveformnetworkid, status) "+
						      " VALUES(?,?,?)");

		getByStatusStmt = connection.prepareStatement(" SELECT waveformid FROM waveformdatabase "+
							      " WHERE status = ? ");

		updateStatusStmt = connection.prepareStatement(" UPDATE waveformdatabase "+
								" SET status = ? ,  "+
								" reason = ? "+
								" WHERE waveformid = ? ");

		statusCountStmt = connection.prepareStatement(" select count(*) from waveformdatabase "+
							      " WHERE status = ? ");

		statusUpdateStmt = connection.prepareStatement(" UPDATE waveformdatabase set status = ? "+ 
							       " WHERE status = ? ");

		successfulChannelCountStmt = connection.prepareStatement(" SELECT count(*) FROM waveformdatabase "+
									 " where waveformeventid = ?  AND "+
									 " ( status = ? OR "+
									 " status = ? ) ");

		deleteByEventIdStmt = connection.prepareStatement(" DELETE FROM waveformdatabase "+
								  " WHERE waveformeventid = ? ");
		

	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}

    }

    public abstract void create();

    public int put(int waveformeventid,
		   int waveformnetworkid) {
	try {
	    int dbid = getId(waveformeventid,
			     waveformnetworkid);
	    if(dbid != -1) return dbid;
	    putStmt.setInt(1, waveformeventid);
	    putStmt.setInt(2, waveformnetworkid);
	    putStmt.setInt(3, Status.NEW.getId());
	    putStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return getId(waveformeventid,
		     waveformnetworkid);
    }
   
    public int getId(int waveformeventid, int waveformnetworkid) {
	try {
	    getIdStmt.setInt(1, waveformeventid);
	    getIdStmt.setInt(2, waveformnetworkid);
	    ResultSet rs = getIdStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public int getFirst() {
	try {
	    getByStatusStmt.setInt(1, Status.NEW.getId());
	    ResultSet rs = getByStatusStmt.executeQuery();
	    if(rs.next()) {
		int rtnValue = rs.getInt(1);
		updateStatus(rtnValue, Status.PROCESSING);
		return rtnValue;
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
	
    }

    public void updateStatus(int waveformid, Status newStatus) {
	updateStatus(waveformid, newStatus, "");
    }

    public void updateStatus(int waveformid, Status newStatus, String reason) {
	try {
	   updateStatusStmt.setInt(1, newStatus.getId());
	   updateStatusStmt.setString(2, reason);
	   updateStatusStmt.setInt(3, waveformid);
	   updateStatusStmt.executeUpdate();
	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}
    }

    public void updateStatus(Status oldStatus, Status newStatus) {
	try {
	    statusUpdateStmt.setInt(1, newStatus.getId());
	    statusUpdateStmt.setInt(2, oldStatus.getId());
	    statusUpdateStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public int[] getByStatus(Status status) {
	ArrayList arrayList = new ArrayList();
	try {
		getByStatusStmt.setInt(1, status.getId());
		ResultSet rs = getByStatusStmt.executeQuery();
		while(rs.next()) {
			arrayList.add(new Integer(rs.getInt(1)));
		}
	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}
	int[] rtnValues = new int[arrayList.size()];
	for(int counter = 0; counter < arrayList.size(); counter++) {
		rtnValues[counter] = ((Integer)arrayList.get(counter)).intValue();
	}
	return rtnValues;
    }


    public int getWaveformEventId(int dbid) {
	
	try {
	    getStmt.setInt(1, dbid);
	    ResultSet rs = getStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("waveformeventid");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }
    
    public int getWaveformNetworkId(int dbid) {
	try {
	    getStmt.setInt(1, dbid);
	    ResultSet rs = getStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("waveformnetworkid");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;

    }

    public int getCount(Status status) {
	try {
	    statusCountStmt.setInt(1, status.getId());
	    ResultSet rs = statusCountStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return 0;
    }

    public int getSuccessfulChannelCount(int waveformEventid) {
	try {
	    successfulChannelCountStmt.setInt(1, waveformEventid);
	    successfulChannelCountStmt.setInt(2, Status.PROCESSING.getId());
	    successfulChannelCountStmt.setInt(3, Status.NEW.getId());
	    
	    ResultSet rs = successfulChannelCountStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public void delete(int waveformEventid) {
	try {
	    deleteByEventIdStmt.setInt(1, waveformEventid);
	    deleteByEventIdStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    

    protected Connection connection;

    private PreparedStatement getStmt;

    private PreparedStatement getIdStmt;

    private PreparedStatement putStmt;

    private PreparedStatement getByStatusStmt;
    
    private PreparedStatement updateStatusStmt;

    private PreparedStatement statusUpdateStmt;

    private PreparedStatement statusCountStmt;

    private PreparedStatement successfulChannelCountStmt;

    private PreparedStatement deleteByEventIdStmt;
    
}// AbstractWaveformDatabase
