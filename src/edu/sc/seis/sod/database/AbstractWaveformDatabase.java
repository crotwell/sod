package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

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
		getStmt = connection.prepareStatement(" SELECT waveformeventid, waveformchannelid "+
						      " FROM waveformchanneldb WHERE waveformid = ? ");

		getIdStmt = connection.prepareStatement(" SELECT waveformid FROM "+
							" waveformchanneldb "+
							" WHERE waveformeventid = ?  AND "+
							" waveformchannelid =? ");


		getByStatusStmt = connection.prepareStatement(" SELECT waveformid FROM waveformchanneldb "+
							      " WHERE status = ? ");

		updateStatusStmt = connection.prepareStatement(" UPDATE waveformchanneldb "+
								" SET status = ? ,  "+
								" reason = ? "+
								" WHERE waveformid = ? ");

		statusUpdateStmt = connection.prepareStatement(" UPDATE waveformchanneldb set status = ? "+ 
							       " WHERE status = ? ");


		deleteByEventIdStmt = connection.prepareStatement(" DELETE FROM waveformchanneldb "+
								  " WHERE waveformeventid = ? ");

		putInfoStmt = connection.prepareStatement(" INSERT INTO waveformdb "+
							  " VALUES(?,?)");
		
		putNetInfoStmt = connection.prepareStatement(" INSERT INTO waveformnetworkdb "+
							     " VALUES(?, ?, ?, ?) ");

		putStationInfoStmt = connection.prepareStatement(" INSERT INTO waveformstationdb "+
								 " VALUES(?, ?, ?, ?)");

		putSiteInfoStmt = connection.prepareStatement(" INSERT INTO waveformsitedb "+
							      " VALUES(?, ?, ?, ?)");

		putChannelInfoStmt = connection.prepareStatement(" INSERT INTO waveformchanneldb "+
								 " (waveformeventid, waveformchannelid, "+
								 " qtime, status, numretrys, reason ) "+
								 " VALUES(?, ?, ?, ?, ?, ?)");

		networkCountStmt = connection.prepareStatement(" UPDATE waveformdb "+
							      " SET numnetworks = numnetworks -1 "+
							      " WHERE waveformeventid = ? ");

		stationCountStmt = connection.prepareStatement(" UPDATE waveformnetworkdb "+
							       " SET numstations = numstations -1 "+
							       " WHERE waveformeventid = ? AND "+
							       " waveformnetworkid = ? ");

		siteCountStmt = connection.prepareStatement(" UPDATE waveformstationdb "+
							    " SET numsites = numsites -1 "+
							    " WHERE waveformeventid = ? AND "+
							    " waveformstationid = ? ");
		
		channelCountStmt = connection.prepareStatement(" UPDATE waveformsitedb "+
							       " SET numchannels = numchannels - 1 "+
							       " WHERE waveformeventid = ? AND "+
							       " waveformsiteid = ? ");

		getNetCountStmt = connection.prepareStatement(" SELECT numnetworks from waveformdb "+
							      " WHERE waveformeventid = ?  ");

		getStationCountStmt = connection.prepareStatement(" SELECT numstations from "+
								  " waveformnetworkdb " +
								  " WHERE waveformeventid = ? AND "+
								  " waveformnetworkid = ? ");
									

		getSiteCountStmt = connection.prepareStatement(" SELECT numsites from "+
							       " waveformstationdb "+
							       " WHERE waveformeventid = ? AND "+
							       " waveformstationid = ? ");

		getChannelCountStmt = connection.prepareStatement(" SELECT numchannels from "+
								  " waveformsitedb "+
								  " WHERE waveformeventid = ? AND "+
								  " waveformsiteid = ? ");

		delInfoStmt = connection.prepareStatement(" DELETE FROM waveformdb "+
							  " WHERE waveformeventid = ? ");

		delNetworkInfoStmt = connection.prepareStatement(" DELETE FROM waveformnetworkdb "+
								 " WHERE waveformeventid = ? AND "+
								 " waveformnetworkid = ? ");

		delStationInfoStmt = connection.prepareStatement( " DELETE FROM waveformstationdb "+
								  " WHERE waveformeventid = ? AND "+
								  " waveformstationid = ? ");

		delSiteInfoStmt = connection.prepareStatement(" DELETE FROM waveformsitedb "+
							      " WHERE waveformeventid = ? AND "+
							      " waveformsiteid = ? ");

		delChannelInfoStmt = connection.prepareStatement(" DELETE FROM waveformchanneldb "+
								 " WHERE waveformeventid = ? AND "+
								 " waveformchannelid = ? ");

		isInfoIns = connection.prepareStatement(" SELECT * from waveformdb "+
							" WHERE waveformeventid = ? ");

		isNetworkInfoIns = connection.prepareStatement(" SELECT * from waveformnetworkdb "+
							       " WHERE waveformeventid = ? AND "+
							       " waveformnetworkid = ? ");

		isStationInfoIns = connection.prepareStatement(" SELECT * from waveformstationdb "+
							       " WHERE waveformeventid = ? AND "+
							       " waveformstationid = ? ");

		isSiteInfoIns = connection.prepareStatement(" SELECT * from waveformsitedb "+
							    " WHERE waveformeventid = ? AND "+
							    " waveformsiteid = ? " );

		isChannelInfoIns = connection.prepareStatement(" SELECT * from waveformchanneldb "+
							       " WHERE waveformeventid = ? AND "+
							       " waveformchannelid = ? ");
		
		getIdsStmt = connection.prepareStatement(" SELECT waveformid from "+
							 " waveformchanneldb "+
							 " WHERE status = ? OR "+
							 " status = ? ");

		deleteStmt = "DELETE FROM ";
		    

	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}

    }

    public abstract void create();

    public int putInfo(int waveformeventid, 
		       int numNetworks) {
	try {
	    if(isInfoInserted(waveformeventid)) return 0;
	    putInfoStmt.setInt(1, waveformeventid);
	    putInfoStmt.setInt(2, numNetworks);
	    putInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return 0;
    
    }

    private boolean isInfoInserted(int waveformeventid) {
	try {
	    isInfoIns.setInt(1, waveformeventid);
	    ResultSet rs = isInfoIns.executeQuery();
	    if(rs.next()) return true;
	} catch(SQLException sqle) {

	}
	return false;
    }

    public int putNetworkInfo(int waveformeventid,
			      int networkid,
			      int numstations,
			      MicroSecondDate date) {
	try {
	    if(isNetworkInfoInserted(waveformeventid,
				     networkid)) {
		return 0;
	    }
	    insert(putNetInfoStmt,
		   1,
		   waveformeventid, 
		   networkid,
		   numstations,
		   date);
	    putNetInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return 0;
    }

    private boolean isNetworkInfoInserted(int waveformeventid,
				     int networkid) {
	try {
	    isNetworkInfoIns.setInt(1, waveformeventid);
	    isNetworkInfoIns.setInt(2, networkid);
	    ResultSet rs = isNetworkInfoIns.executeQuery();
	    if(rs.next()) return true;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return false;
    }

    public int putStationInfo(int waveformeventid, 
 			      int stationid,
			      int numsites,
			      MicroSecondDate date) {
	try {
	    if(isStationInfoInserted(waveformeventid,
				     stationid)) return 0;
	    insert(putStationInfoStmt,
		   1,
		   waveformeventid, 
		   stationid,
		   numsites,
		   date);
	    putStationInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return 0;
    }

    private boolean isStationInfoInserted(int waveformeventid,
				     int stationid) {
	try {
	    isStationInfoIns.setInt(1,waveformeventid);
	    isStationInfoIns.setInt(2, stationid);
	    ResultSet rs = isStationInfoIns.executeQuery();
	    if(rs.next()) return true;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return false;
    }

    public int putSiteInfo(int waveformeventid, 
			   int siteid,
			   int numchannels,
			   MicroSecondDate date) {
	try {
	   //  if(siteid == 5){  System.out.println("siteid is 5");
// 	    // System.exit(0);
// 	    }
	    
	    if(isSiteInfoInserted(waveformeventid,
				  siteid)) return 0;
	  //   if(siteid == 5){  System.out.println("siteid is 5 after ischeck");
// 	    System.exit(0);
// 	    }
	    
					       
	    insert(putSiteInfoStmt,
		   1,
		   waveformeventid, 
		   siteid,
		   numchannels,
		   date);
	    putSiteInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return 0;
    }

    public boolean isSiteInfoInserted(int waveformeventid,
				 int siteid) {
	try {
	    isSiteInfoIns.setInt(1, waveformeventid);
	    isSiteInfoIns.setInt(2, siteid);
	    ResultSet rs = isSiteInfoIns.executeQuery();
	    if(rs.next()) return true;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return false;
    }
  
    public int putChannelInfo(int waveformeventid, 
			      int channelid,
			      MicroSecondDate date) {
	try {
	    if(isChannelInfoInserted(waveformeventid,
				     channelid)) return 0;
	    putChannelInfoStmt.setInt(1, waveformeventid);
	    putChannelInfoStmt.setInt(2, channelid);
	    putChannelInfoStmt.setTimestamp(3, date.getTimestamp());
	    putChannelInfoStmt.setInt(4, Status.NEW.getId());
	    putChannelInfoStmt.setInt(5, 0);
	    putChannelInfoStmt.setString(6, "");
	    putChannelInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return 0;
    }

    private boolean isChannelInfoInserted(int waveformeventid,
					  int channelid) {
	try {
	    isChannelInfoIns.setInt(1, waveformeventid);
	    isChannelInfoIns.setInt(2, channelid);
	    ResultSet rs = isChannelInfoIns.executeQuery();
	    if(rs.next()) return true;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return false;
    }

    public void decrementNetworkCount(int waveformeventid) {
	try {
	    networkCountStmt.setInt(1, waveformeventid);
	    networkCountStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void decrementStationCount(int waveformeventid, int networkid) {
	try {
	    stationCountStmt.setInt(1, waveformeventid);
	    stationCountStmt.setInt(2, networkid);	
	    stationCountStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void decrementSiteCount(int waveformeventid, int stationid) {
	try {
	    siteCountStmt.setInt(1, waveformeventid);
	    siteCountStmt.setInt(2, stationid);
	    siteCountStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void decrementChannelCount(int waveformeventid, int siteid) {
	try {
	    channelCountStmt.setInt(1, waveformeventid);
	    channelCountStmt.setInt(2, siteid);
	    channelCountStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

  
    public int getNetworkCount(int waveformeventid) {
	try {
		getNetCountStmt.setInt(1, waveformeventid);
		ResultSet rs = getNetCountStmt.executeQuery();
		if(rs.next())  return rs.getInt(1);
	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}
	return -1;
    }
    
    public int getStationCount(int waveformeventid, int networkid) {
	try {
		getStationCountStmt.setInt(1, waveformeventid);
		getStationCountStmt.setInt(2, networkid);
		ResultSet rs = getStationCountStmt.executeQuery();
		if(rs.next()) return rs.getInt(1);	
	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}
	return -1;
    }

    public int getSiteCount(int waveformeventid, int stationid) {
	try {
		getSiteCountStmt.setInt(1, waveformeventid);
		getSiteCountStmt.setInt(2, stationid);
		ResultSet rs = getSiteCountStmt.executeQuery();
		if(rs.next()) return rs.getInt(1);
	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}
	return -1;
    }

    public int getChannelCount(int waveformeventid, int siteid) {
	try {
		getChannelCountStmt.setInt(1, waveformeventid); 
		getChannelCountStmt.setInt(2, siteid);
		ResultSet rs = getChannelCountStmt.executeQuery();
		if(rs.next()) return rs.getInt(1);
	} catch(SQLException sqle) {
		sqle.printStackTrace();
	}
	return -1;
    }
    

    private int insert(PreparedStatement stmt, 
		       int index,
		       int waveformeventid,
		       int refdbid,
		       int numentries,
		       MicroSecondDate date) {
	try {
	    stmt.setInt(index++, waveformeventid);
	    stmt.setInt(index++, refdbid);
	    stmt.setInt(index++, numentries);
	    stmt.setTimestamp(index++, date.getTimestamp());
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return index;
    }
   
    public int getChannelDbId(int waveformeventid, int waveformchannelid) {

	try {
	    getIdStmt.setInt(1, waveformeventid);
	    getIdStmt.setInt(2, waveformchannelid);
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
    
    public int getWaveformChannelId(int dbid) {
	try {
	    getStmt.setInt(1, dbid);
	    ResultSet rs = getStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("waveformchannelid");
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
    

    public void deleteInfo(int waveformeventid) {
	try {
	    delInfoStmt.setInt(1, waveformeventid);
	    delInfoStmt.executeUpdate();

	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void deleteNetworkInfo(int waveformeventid,
				  int networkid) {
	try {
	    delNetworkInfoStmt.setInt(1, waveformeventid);
	    delNetworkInfoStmt.setInt(2, networkid);
	    delNetworkInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void deleteStationInfo(int waveformeventid, 
				  int stationid) {

	try {
	    delStationInfoStmt.setInt(1, waveformeventid);
	    delStationInfoStmt.setInt(2, stationid);
	    delStationInfoStmt.executeUpdate();
	} catch(SQLException sqle) { 
	    sqle.printStackTrace();
	}
    }


    public void deleteSiteInfo(int waveformeventid,
			       int siteid) {
	try {
	    delSiteInfoStmt.setInt(1, waveformeventid);
	    delSiteInfoStmt.setInt(2, siteid);
	    delSiteInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void deleteChannelInfo(int waveformeventid,
				  int channelid) {
	try {
	    delChannelInfoStmt.setInt(1, waveformeventid);
	    delChannelInfoStmt.setInt(2, channelid);
	    delChannelInfoStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public int[] getIds() {
	ArrayList arrayList = new ArrayList();
	try {
	    getIdsStmt.setInt(1, Status.COMPLETE_SUCCESS.getId());
	    getIdsStmt.setInt(2, Status.COMPLETE_REJECT.getId());
	    ResultSet rs = getIdsStmt.executeQuery();
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

    public void delete(String tableName) {
	try {
	    connection.createStatement().execute(deleteStmt+tableName);
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void clean() {
	delete("waveformdb");
	delete("waveformnetworkdb");
	delete("waveformstationdb");
	delete("waveformsitedb");
	delete("waveformchanneldb");
    }

    protected Connection connection;

    private PreparedStatement getStmt;

    private PreparedStatement getIdStmt;

    private PreparedStatement getByStatusStmt;
    
    private PreparedStatement updateStatusStmt;

    private PreparedStatement statusUpdateStmt;

    private PreparedStatement deleteByEventIdStmt;

    private PreparedStatement putInfoStmt;
    
    private PreparedStatement putNetInfoStmt;

    private PreparedStatement putStationInfoStmt;

    private PreparedStatement putSiteInfoStmt;

    private PreparedStatement putChannelInfoStmt;

    private PreparedStatement channelCountStmt;;

    private PreparedStatement networkCountStmt;

    private PreparedStatement stationCountStmt;

    private PreparedStatement siteCountStmt;

    private PreparedStatement getNetCountStmt;

    private PreparedStatement getStationCountStmt;

    private PreparedStatement getSiteCountStmt;

    private PreparedStatement getChannelCountStmt;

    private PreparedStatement delInfoStmt;

    private PreparedStatement delNetworkInfoStmt;

    private PreparedStatement delStationInfoStmt;

    private PreparedStatement delSiteInfoStmt;

    private PreparedStatement delChannelInfoStmt;

    private PreparedStatement isInfoIns;

    private PreparedStatement isNetworkInfoIns;

    private PreparedStatement isStationInfoIns;

    private PreparedStatement isSiteInfoIns;

    private PreparedStatement isChannelInfoIns;

     private PreparedStatement getIdsStmt; 

    private String deleteStmt;

    static Category logger = 
        Category.getInstance(AbstractWaveformDatabase.class.getName());
    
}// AbstractWaveformDatabase
