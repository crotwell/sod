package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfNetwork.*;

import java.sql.*;

/**
 * AbstractNetworkDatabase.java
 *
 *
 * Created: Tue Oct  8 15:31:28 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract  class AbstractNetworkDatabase implements NetworkDatabase{
    public AbstractNetworkDatabase (Connection connection){
	this.connection = connection;
	init();
    }

    public abstract void create();
    
    private void init() {
	try {
	    create();
	     getStmt = connection.prepareStatement("SELECT serverName, "+
						  " serverDNS, "+
						  " network_code, "+
						  " station_code, "+
						  " site_code, "+
						  " channel_code, "+
						  " network_time, "+
						  " channel_time "+
						  " FROM networkdatabase "+
						  " WHERE networkid = ? ");
	    
	    putStmt = connection.prepareStatement(" INSERT INTO networkdatabase "+
						  " (serverName, "+
						  " serverDNS, "+
						  " network_code, "+
						  " station_code, "+
						  " site_code, "+
						  " channel_code, "+
						  " network_time, "+
						  " channel_time, "+
						  " channelIdIOR) "+
						  " VALUES "+
						  " (?, ?, ?, ?, ?, ?, ?, ?, ?) ");

	    getIdStmt = connection.prepareStatement(" SELECT networkid FROM networkdatabase "+
						    " WHERE serverName = ? AND "+
						    " serverDNS = ? AND " +
						    " network_code = ? AND "+
						    " station_code = ? AND "+
						    " site_code = ? AND "+
						    " channel_code = ? AND "+
						    " network_time = ? AND "+
						    " channel_time = ? ");
						    

	} catch(Exception e) {
	    e.printStackTrace();
	}

    }
    
    private void put(String serverName,
		    String serverDNS,
		    String network_code,
		    String station_code,
		    String site_code,
		    String channel_code,
		    edu.iris.Fissures.Time network_time,
		    edu.iris.Fissures.Time channel_time,
		    String channelIdIOR) {
	try {
	    int index = insert(putStmt,
			       1,
			       serverName,
			       serverDNS,
			       network_code,
			       station_code,
			       site_code,
			       channel_code,
			       network_time,
			       channel_time);
	    String ior = new String("test");
	    putStmt.setString(index++, ior);
	    putStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
		       
    }

    public int put(String serverName,
		    String serverDNS,
		    Channel channel) {
	int dbid = getId(serverName,
			 serverDNS,
			 channel);
	if(dbid != -1) return dbid;
	String network_code = channel.get_id().network_id.network_code;
	String station_code =  channel.get_id().station_code;
	String site_code = channel.get_id().site_code;
	String channel_code = channel.get_id().channel_code;
	edu.iris.Fissures.Time network_time = channel.get_id().network_id.begin_time;
	edu.iris.Fissures.Time channel_time = channel.get_id().begin_time;
	put(serverName,
	    serverDNS,
	    network_code,
	    station_code,
	    site_code,
	    channel_code,
	    network_time,
	    channel_time,
	    "test");
	return getId(serverName, 
		     serverDNS,
		     channel);
	    
    }
    
    private int insert(PreparedStatement stmt,
		       int index,
		       String serverName,
		       String serverDNS,
		       String network_code,
		       String station_code,
		       String site_code,
		       String channel_code,
		       edu.iris.Fissures.Time network_time,
		       edu.iris.Fissures.Time channel_time) {
	try {
	    stmt.setString(index++, serverName);
	    stmt.setString(index++, serverDNS);
	    stmt.setString(index++, network_code);
	    stmt.setString(index++, station_code);
	    stmt.setString(index++, site_code);
	    stmt.setString(index++, channel_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(network_time);
	    stmt.setTimestamp(index++, microSecondDate.getTimestamp());
	    microSecondDate = new MicroSecondDate(channel_time);
	    stmt.setTimestamp(index++, microSecondDate.getTimestamp());
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return index;
    }
    
    public int getId(String serverName,
		   String serverDNS,
		   String network_code,
		   String station_code,
		   String site_code,
		   String channel_code,
		   edu.iris.Fissures.Time network_time,
		   edu.iris.Fissures.Time channel_time) {
	try {
	    insert(getIdStmt,
		   1,
		   serverName,
		   serverDNS,
		   network_code,
		   station_code,
		   site_code,
		   channel_code,
		   network_time,
		   channel_time);
	    ResultSet rs = getIdStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("networkid");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;

    }

    public int getId(String serverName,
		     String serverDNS,
		     Channel channel) {
	String network_code = channel.get_id().network_id.network_code;
	String station_code =  channel.get_id().station_code;
	String site_code = channel.get_id().site_code;
	String channel_code = channel.get_id().channel_code;
	edu.iris.Fissures.Time network_time = channel.get_id().network_id.begin_time;
	edu.iris.Fissures.Time channel_time = channel.get_id().begin_time;
	return getId(serverName,
	      serverDNS,
	      network_code,
	      station_code,
	      site_code,
	      channel_code,
	      network_time,
	      channel_time);
	      
    }


    protected Connection connection;

    private PreparedStatement getStmt;
    
    private PreparedStatement putStmt;
    
    private PreparedStatement getIdStmt;
    
}// AbstractNetworkDatabase
