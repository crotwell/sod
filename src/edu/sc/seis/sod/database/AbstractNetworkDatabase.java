package edu.sc.seis.sod.database;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfNetwork.*;

import java.sql.*;
import java.util.*;
import org.omg.CORBA.*;

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

    public abstract ConfigDatabase getConfigDatabase();
    
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
						  " channel_time, "+
						  " nleapseconds, "+
						  " cleapseconds "+
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
						  " nleapseconds, "+
						  " cleapseconds, "+
						  " channelIdIOR) "+
						  " VALUES "+
						  " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

	    getIdStmt = connection.prepareStatement(" SELECT networkid FROM networkdatabase "+
						    " WHERE serverName = ? AND "+
						    " serverDNS = ? AND " +
						    " network_code = ? AND "+
						    " station_code = ? AND "+
						    " site_code = ? AND "+
						    " channel_code = ? AND "+
						    " network_time = ? AND "+
						    " channel_time = ? ");


	    getChannelsStmt = connection.prepareStatement(" SELECT networkid, channelIdIOR FROM networkdatabase ");
						    

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
	    
	    putStmt.setInt(index++, network_time.leap_seconds_version);
	    putStmt.setInt(index++, channel_time.leap_seconds_version);
	    
	    String ior = new String("test");
	    putStmt.setString(index++, channelIdIOR);
	   	    putStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
		       
    }

    public int put(String serverName,
		   String serverDNS,
		   Channel channel,
		   NetworkAccess networkAccess) {
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
	String networkAccessIor = null;
	try { 
	    org.omg.CORBA_2_3.ORB orb = CommonAccess.getCommonAccess().getORB();
	    networkAccessIor = orb.object_to_string((org.omg.CORBA.Object)networkAccess);
	} catch(ConfigurationException cfe) {
	    cfe.printStackTrace();
	}
	put(serverName,
	    serverDNS,
	    network_code,
	    station_code,
	    site_code,
	    channel_code,
	    network_time,
	    channel_time,
	    networkAccessIor);
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

    public String getTimeConfigName() {
	return "networktimeconfig";
    }

    public void setTime(String serverName, String serverDNS, edu.iris.Fissures.Time time) {
	getConfigDatabase().setTime(serverName, serverDNS, time);
    }
    
    public edu.iris.Fissures.Time getTime(String serverName, String serverDNS) {
	return getConfigDatabase().getTime(serverName,
					   serverDNS);
    }
    
    public void incrementTime(String serverName, String serverDNS, int numDays) {
	getConfigDatabase().incrementTime(serverName,
					  serverDNS,
					  numDays);
    }


    public Channel[] getChannels() {
	try {
	    ResultSet rs = getChannelsStmt.executeQuery();
	    ArrayList arrayList = new ArrayList();
	    while(rs.next()) {
		String ior = rs.getString("channelIdIOR");
		//System.out.println("ChannelId that is obtained is "+ior);
		org.omg.CORBA.ORB orb = CommonAccess.getCommonAccess().getORB();
		org.omg.CORBA.Object obj = orb.string_to_object(ior);
		NetworkAccess networkAccess = NetworkAccessHelper.narrow(obj);
		// here I must revalidate
		int dbid = rs.getInt("networkid");
		//System.out.println("before retrieveing the channel");
		//System.out.println(networkAccess.get_attributes().name);
		Channel channel = networkAccess.retrieve_channel(getChannelId(dbid));
		//System.out.println("After retrieveing the channel");
		arrayList.add(channel);
		
	    }
	    
	    
	    Channel[] rtnValues = new Channel[arrayList.size()];
	    rtnValues = (Channel[]) arrayList.toArray(rtnValues);
	    return rtnValues;
	} catch(Exception e) {
	    e.printStackTrace();
	    return new Channel[0];
	}
    }

    public NetworkId getNetworkId(int dbid) {
	String network_code = getNetworkCode(dbid);
	edu.iris.Fissures.Time network_time = getNetworkTime(dbid);
	return new NetworkId(network_code, network_time);
    }

    private String getNetworkCode(int dbid) {
	return (String)getField(3, dbid);
    }

    private String getStationCode(int dbid) {
	return (String)getField(4, dbid);
    }

    private String getSiteCode(int dbid) {
	return (String)getField(5, dbid);
    }

    private String getChannelCode(int dbid) {
	return (String)getField(6, dbid);
    }

    private edu.iris.Fissures.Time getNetworkTime(int dbid) {
	edu.iris.Fissures.Time rtnTime = (edu.iris.Fissures.Time)getField(7, dbid);
	rtnTime.leap_seconds_version = ((Integer)getField(9, dbid)).intValue();
	return rtnTime;

    }
    
    private edu.iris.Fissures.Time getChannelTime(int dbid) {
	edu.iris.Fissures.Time rtnTime = (edu.iris.Fissures.Time)getField(8, dbid);
	rtnTime.leap_seconds_version = ((Integer)getField(10, dbid)).intValue();
	return rtnTime;
    }

    

    public ChannelId getChannelId(int dbid) {

	String station_code = getStationCode(dbid);
	String site_code = getSiteCode(dbid);
	String channel_code = getChannelCode(dbid);
	edu.iris.Fissures.Time channel_time = getChannelTime(dbid);
	NetworkId networkId = getNetworkId(dbid);
	return new ChannelId(networkId,
			     station_code,
			     site_code,
			     channel_code,
			     channel_time);
			     
    }

    

    private java.lang.Object getField(int index, int dbid) {
	try {
	    getStmt.setInt(1, dbid);
	    ResultSet rs = getStmt.executeQuery();
	    if(rs.next()) {
		switch(index) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:  
		    return rs.getString(index);
		    
		case 7:
		case 8:  
		    return (new MicroSecondDate(rs.getTimestamp(index))).getFissuresTime();
		case 9:
		case 10:
		    return new Integer(rs.getInt(index));
		    
		}
		
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	    
	}
	return null;
    }


    protected Connection connection;

    private PreparedStatement getStmt;
    
    private PreparedStatement putStmt;
    
    private PreparedStatement getIdStmt;

    private PreparedStatement getChannelsStmt;
    
}// AbstractNetworkDatabase
