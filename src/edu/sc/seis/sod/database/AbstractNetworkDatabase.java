package edu.sc.seis.sod.database;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfNetwork.*;

import java.sql.*;
import java.util.*;
import org.omg.CORBA.*;
import org.apache.log4j.*;

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
	    
	    netPutStmt = connection.prepareStatement("INSERT INTO networkdatabase "+
						    " ( serverName, "+
						    " serverDNS, "+
						    " network_code, "+
						    " network_time, "+
						    " nleapseconds, "+
						    " nqtime, "+
						    " status, "+
						    " networkAccessIOR ) "+
						    " VALUES(?,?,?,?,?,?,?,?)");

	    netIdStmt = connection.prepareStatement(" SELECT networkid FROM networkdatabase "+
						     " WHERE network_code = ? AND "+
						     " network_time = ? AND "+
						     " nleapseconds = ? ");
						     
	    
	    stationPutStmt = connection.prepareStatement("INSERT INTO stationdatabase "+
							 " (networkid, "+
							 " station_code, "+
							 " station_time, "+
							 " stleapseconds, "+
							 " stqtime, "+
							 " status )"+
							 " VALUES(?,?,?,?,?,?)");
	    
	    stationIdStmt =  connection.prepareStatement("SELECT stationid FROM stationdatabase "+
							 " WHERE networkid = ? AND "+
							 " station_code = ?  AND "+
							 " station_time = ? AND "+
							 " stleapseconds = ? ");
	    sitePutStmt = connection.prepareStatement("INSERT INTO sitedatabase "+
						      " ( stationid, "+
						      " site_code, "+
						      " site_time, "+
						      " sleapseconds, "+
						      " sqtime, "+
						      " status ) "+
						      " VALUES(?,?,?,?,?,?)");
	    siteIdStmt = connection.prepareStatement(" SELECT siteid FROM sitedatabase "+
						     " WHERE stationid = ? AND "+
						     " site_code = ? AND "+
						     " site_time = ? AND "+
						     " sleapseconds = ? ");
	    

	    channelPutStmt = connection.prepareStatement(" INSERT INTO channeldatabase "+
							 " ( siteid, "+
							 " channel_code, "+
							 " channel_time, "+
							 " cleapseconds, "+
							 " cqtime, "+
							 " status ) "+
							 " VALUES(?, ?, ?, ?, ?, ?)");
	    
	    channelIdStmt = connection.prepareStatement(" SELECT channelid FROM channeldatabase "+
							" WHERE siteid = ? AND "+
							" channel_code = ?  AND "+
							" channel_time = ? AND "+
							" cleapseconds = ? ");

	    getNetStmt = connection.prepareStatement(" SELECT network_code, "+
						     " network_time, "+
						     " nleapseconds, "+
						     " networkAccessIOR "+
						     " FROM networkdatabase "+
						     " WHERE networkid = ? ");
	    
	    getStationStmt = connection.prepareStatement(" SELECT station_code, "+
							 " station_time, "+
							 " stleapseconds, "+
							 " networkid "+
							 " FROM stationdatabase "+
							 " WHERE stationid = ? ");
	    
	    getSiteStmt = connection.prepareStatement(" SELECT site_code, "+
						      " site_time, "+
						      " sleapseconds, "+
						      " stationid "+
						      " FROM sitedatabase "+
						      " WHERE siteid = ? ");
	    
	    getChannelStmt = connection.prepareStatement(" SELECT channel_code, "+
							 " channel_time,  "+
							 " cleapseconds, "+
							 " siteid "+
							 " FROM channeldatabase "+
							 " WHERE channelid = ? ");

	    networkIdsStmt = connection.prepareStatement(" SELECT networkid FROM "+
							    " networkdatabase ");

	    stationIdsStmt = connection.prepareStatement(" SELECT stationid FROM "+
							    " stationdatabase "+
							    " WHERE networkid = ? ");
	    
	    siteIdsStmt = connection.prepareStatement(" SELECT siteid FROM "+
							 " sitedatabase "+
							 " WHERE stationid = ? ");

	    channelIdsStmt = connection.prepareStatement(" SELECT channelid FROM "+
							    " channeldatabase "+
							    " WHERE siteid = ? ");

	    deleteStmt = " DELETE FROM ";
	    
	} catch(Exception e) {
	    e.printStackTrace();
	}

    }

    

    public int putNetwork(String serverName,
			  String serverDNS,
			  NetworkAccess networkAccess) {
	try {
	    int dbid = getNetworkDbId(networkAccess);
	    if(dbid != -1) return dbid;
	    NetworkId networkId = networkAccess.get_attributes().get_id();
	    if(netPutStmt == null) System.out.println("netPUtStmt is null");
	    else System.out.println("NetputStmt is not NULLL");
	    netPutStmt.setString(1, serverName);
	    netPutStmt.setString(2, serverDNS);
	    netPutStmt.setString(3, networkId.network_code);
	    MicroSecondDate microSecondDate  = new MicroSecondDate(networkId.begin_time);
	    netPutStmt.setTimestamp(4, microSecondDate.getTimestamp());
	    netPutStmt.setInt(5, networkId.begin_time.leap_seconds_version);
	    netPutStmt.setTimestamp(6, (new MicroSecondDate()).getTimestamp());
	    netPutStmt.setInt(7, Status.NEW.getId());
	    String networkAccessIor = null;
	    try { 
		org.omg.CORBA_2_3.ORB orb = CommonAccess.getCommonAccess().getORB();
		networkAccessIor = orb.object_to_string((org.omg.CORBA.Object)networkAccess);
	    } catch(ConfigurationException cfe) {
		cfe.printStackTrace();
	    }
	    netPutStmt.setString(8, networkAccessIor);
	    netPutStmt.executeUpdate();
	    return getNetworkDbId(networkAccess);
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public int getNetworkDbId(NetworkAccess networkAccess) {
	try {
	    NetworkId networkId = networkAccess.get_attributes().get_id();
	    netIdStmt.setString(1, networkId.network_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(networkId.begin_time);
	    netIdStmt.setTimestamp(2, microSecondDate.getTimestamp());
	    netIdStmt.setInt(3, networkId.begin_time.leap_seconds_version);
	    ResultSet rs = netIdStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public int putStation(NetworkDbObject networkDbObject, Station station) {
	try {
	    int dbid = getStationDbId(networkDbObject, station);
	    if(dbid != -1) return dbid;
	    int networkdbid = networkDbObject.getDbId();
	    stationPutStmt.setInt(1, networkdbid);
	    StationId stationId = station.get_id();
	    stationPutStmt.setString(2, stationId.station_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(stationId.begin_time);
	    stationPutStmt.setTimestamp(3, microSecondDate.getTimestamp());
	    stationPutStmt.setInt(4, stationId.begin_time.leap_seconds_version);
	    stationPutStmt.setTimestamp(5, (new MicroSecondDate()).getTimestamp());
	    stationPutStmt.setInt(6, Status.NEW.getId());
	    stationPutStmt.executeUpdate();
	    return getStationDbId(networkDbObject, station);
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
					
	return -1;
    }

   
    
    public int getStationDbId(NetworkDbObject networkDbObject, Station station) {
	try {
	    int networkdbid = networkDbObject.getDbId();
	    stationIdStmt.setInt(1, networkdbid);
	    StationId stationId = station.get_id();
	    stationIdStmt.setString(2, stationId.station_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(stationId.begin_time);
	    stationIdStmt.setTimestamp(3, microSecondDate.getTimestamp());
	    stationIdStmt.setInt(4, stationId.begin_time.leap_seconds_version);
	    ResultSet rs = stationIdStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public int putSite(StationDbObject stationDbObject,
		       Site site) {

	try {
	    int dbid = getSiteDbId(stationDbObject,
				   site);
	    if(dbid != -1) return dbid;
	    
	    int stationdbid = stationDbObject.getDbId();
	    sitePutStmt.setInt(1, stationdbid);
	    SiteId siteId =  site.get_id();
	    sitePutStmt.setString(2, siteId.site_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(siteId.begin_time);
	    sitePutStmt.setTimestamp(3, microSecondDate.getTimestamp());
	    sitePutStmt.setInt(4, siteId.begin_time.leap_seconds_version);
	    sitePutStmt.setTimestamp(5, (new MicroSecondDate()).getTimestamp());
	    sitePutStmt.setInt(6, Status.NEW.getId());
	    sitePutStmt.executeUpdate();
	    return getSiteDbId(stationDbObject,
			       site);
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }
    
    public int getSiteDbId(StationDbObject stationDbObject,
			   Site site) {
	
	try {
	    int stationdbid = stationDbObject.getDbId();
	    siteIdStmt.setInt(1, stationdbid);
	    SiteId siteId = site.get_id();
	    siteIdStmt.setString(2, siteId.site_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(siteId.begin_time);
	    siteIdStmt.setTimestamp(3, microSecondDate.getTimestamp());
	    siteIdStmt.setInt(4, siteId.begin_time.leap_seconds_version);
	    ResultSet rs = siteIdStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
	
    }

    public int putChannel(SiteDbObject siteDbObject,
			  Channel  channel) {

	try {
	    int dbid = getChannelDbId(siteDbObject,
				      channel);
	    System.out.println("**** INSETING CHANNEL INTO DATABASE "+dbid);
	    if(dbid != -1) return dbid;
	    channelPutStmt.setInt(1, siteDbObject.getDbId());
	    ChannelId channelId = channel.get_id();
	    channelPutStmt.setString(2, channelId.channel_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(channelId.begin_time);
	    channelPutStmt.setTimestamp(3, microSecondDate.getTimestamp());
	    channelPutStmt.setInt(4, channelId.begin_time.leap_seconds_version);
	    channelPutStmt.setTimestamp(5, (new MicroSecondDate()).getTimestamp());
	    channelPutStmt.setInt(6,  Status.NEW.getId());
	    channelPutStmt.executeUpdate();
	    return getChannelDbId(siteDbObject,
				  channel);
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public int getChannelDbId(SiteDbObject siteDbObject,
			      Channel channel) {

	try {
	    channelIdStmt.setInt(1, siteDbObject.getDbId());
	    ChannelId channelId = channel.get_id();
	    channelIdStmt.setString(2, channelId.channel_code);
	    MicroSecondDate microSecondDate = new MicroSecondDate(channelId.begin_time);
	    channelIdStmt.setTimestamp(3, microSecondDate.getTimestamp());
	    channelIdStmt.setInt(4, channelId.begin_time.leap_seconds_version);
	    ResultSet rs = channelIdStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }


    public int getSiteDbId(int channelDbId) {
	try {

	    getChannelStmt.setInt(1, channelDbId);
	    ResultSet rs = getChannelStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("siteid");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public int getStationDbId(int siteDbId) {
	try {
	    getSiteStmt.setInt(1, siteDbId);
	    ResultSet rs = getSiteStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("stationid");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public int getNetworkDbId(int stationDbId) {
	try {
	    getStationStmt.setInt(1, stationDbId);
	    ResultSet rs = getStationStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt("networkid");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return -1;
    }

    public NetworkAccess getNetworkAccess(int networkid) {
	try {
	    
	    //dont forget to revalidate the obtained networkAccess..
	    getNetStmt.setInt(1, networkid);
	    ResultSet rs = getNetStmt.executeQuery();
	    if(rs.next()) {
		String ior = rs.getString("networkAccessIOR");
		//System.out.println("ChannelId that is obtained is "+ior);
		org.omg.CORBA.ORB orb = CommonAccess.getCommonAccess().getORB();
		org.omg.CORBA.Object obj = orb.string_to_object(ior);
		NetworkAccess networkAccess = NetworkAccessHelper.narrow(obj);
		return networkAccess;
	    }
	} catch(Exception sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }


    public NetworkId getNetworkId(int networkid) {
	try {
	    getNetStmt.setInt(1, networkid);
	    ResultSet rs = getNetStmt.executeQuery();
	    if(rs.next()) {
		edu.iris.Fissures.Time networkTime = new MicroSecondDate(rs.getTimestamp("network_time")).getFissuresTime();
		networkTime.leap_seconds_version = rs.getInt("nleapseconds");
		String network_code =  rs.getString("network_code");
		return new NetworkId(network_code,
				     networkTime);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }

    public StationId getStationId(int stationid) {
	try {
	    int networkid = getNetworkDbId(stationid);
	    NetworkId networkId = getNetworkId(networkid);
	    getStationStmt.setInt(1, stationid);
	    ResultSet rs = getStationStmt.executeQuery();
	    if(rs.next()) {
		String station_code = rs.getString("station_code");
		edu.iris.Fissures.Time stationTime = new MicroSecondDate(rs.getTimestamp("station_time")).getFissuresTime();
		stationTime.leap_seconds_version = rs.getInt("stleapseconds");
		return new StationId(networkId,
				     station_code,
				     stationTime);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }
    public Station getStation(int stationid) {

	try {
	    getStationStmt.setInt(1, stationid);
	    ResultSet rs = getStationStmt.executeQuery();
	    if(rs.next()) {
		Site site = getSite(rs.getInt("siteid"));
		if(site != null) {
		    return site.my_station;
		}
	    }
	    
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }


    public Site getSite(int siteid) {

	int stationid = getStationDbId(siteid);
	int networkid = getNetworkDbId(stationid);
	StationId stationId = getStationId(stationid);
	NetworkAccess networkAccess = getNetworkAccess(networkid);
	Channel[] channels = networkAccess.retrieve_for_station(stationId);
	if(channels.length != 0) {
	    return channels[0].my_site;
	}
	return null;
    }
    
    public SiteId getSiteId(int siteid) {
	try {
	    int stationid = getStationDbId(siteid);
	    StationId stationId = getStationId(stationid);
	    NetworkId networkId = stationId.network_id;
	    getSiteStmt.setInt(1, siteid);
	    ResultSet rs = getSiteStmt.executeQuery();
	    if(rs.next()) {
		edu.iris.Fissures.Time siteTime = new MicroSecondDate(rs.getTimestamp("site_time")).getFissuresTime();
		siteTime.leap_seconds_version = rs.getInt("sleapseconds");
	    
		return new SiteId(networkId,
				  stationId.station_code,
				  rs.getString("site_code"),
				  siteTime);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }

    public Channel getChannel(int channelid) {
	int siteid = getSiteDbId(channelid);
	int stationid = getStationDbId(siteid);
	int networkid = getNetworkDbId(stationid);
	NetworkAccess networkAccess = getNetworkAccess(networkid);
	ChannelId channelId = getChannelId(channelid);
	try {
	    Channel channel = networkAccess.retrieve_channel(channelId);
	    return channel;
	} catch(ChannelNotFound cnf) {
	    cnf.printStackTrace();
	}
	return null;
    }
    
    public ChannelId getChannelId(int channelid) {
	try {
	    int siteid = getSiteDbId(channelid);
	    SiteId siteId = getSiteId(siteid);
	    NetworkId networkId = siteId.network_id;
	    
	    getChannelStmt.setInt(1, channelid);
	    ResultSet rs = getChannelStmt.executeQuery();
	    if(rs.next()) {
		edu.iris.Fissures.Time channelTime = new MicroSecondDate(rs.getTimestamp("channel_time")).getFissuresTime();
		channelTime.leap_seconds_version = rs.getInt("cleapseconds");
		return new ChannelId(networkId,
				     siteId.station_code,
				     siteId.site_code,
				     rs.getString("channel_code"),
				     channelTime);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }


    public int[] getNetworkDbIds() {
	ArrayList arrayList = new ArrayList();
	try {
	    ResultSet rs = networkIdsStmt.executeQuery();
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

    public int[] getStationDbIds(int networkid) {
	ArrayList arrayList = new ArrayList();
	try {
	    stationIdsStmt.setInt(1, networkid);
	    ResultSet rs = stationIdsStmt.executeQuery();
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

    public int[] getSiteDbIds(int stationid) {
	ArrayList arrayList = new ArrayList();
	try {
	    siteIdsStmt.setInt(1, stationid);
	    ResultSet rs = siteIdsStmt.executeQuery();
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
    
    public int[] getChannelDbIds(int siteid) {
	ArrayList arrayList = new ArrayList();
	try {
	    channelIdsStmt.setInt(1, siteid);
	    ResultSet rs = channelIdsStmt.executeQuery();
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
    
    public NetworkDbObject[] getNetworks() {
	int[] ids = getNetworkDbIds();
	NetworkDbObject[] rtnValues = new NetworkDbObject[ids.length];
	for(int counter = 0; counter < ids.length; counter++) {
	    rtnValues[counter] = new NetworkDbObject(ids[counter],
						     getNetworkAccess(ids[counter]));
	}
	return rtnValues;
    }

    public StationDbObject[] getStations(int networkid) {
	int[] ids = getStationDbIds(networkid);
	StationDbObject[] rtnValues = new StationDbObject[ids.length];
	for(int counter = 0; counter < ids.length; counter++) {
	    rtnValues[counter] = new StationDbObject(ids[counter],
						     getStation(ids[counter]));
	}
	return rtnValues;
    }

    public SiteDbObject[] getSites(int stationid) {
	int[] ids = getSiteDbIds(stationid);
	SiteDbObject[] rtnValues = new SiteDbObject[ids.length];
	for(int counter = 0; counter < ids.length; counter++) {
	    rtnValues[counter] = new SiteDbObject(ids[counter],
						  getSite(ids[counter]));
	}
	return rtnValues;
    }

    public ChannelDbObject[] getChannels(int siteid) {
	int[] ids = getChannelDbIds(siteid);
	ChannelDbObject[] rtnValues = new ChannelDbObject[ids.length];
	for(int counter = 0; counter < ids.length; counter++) {
	    rtnValues[counter] = new ChannelDbObject(ids[counter],
						     getChannel(ids[counter]));
	}
	return rtnValues;
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

    public void delete(String tableName) {
	try {
	    connection.createStatement().execute(deleteStmt+tableName);
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void clean() {
	delete("networkdatabase");
	delete("stationdatabase");
	delete("sitedatabase");
	delete("channeldatabase");
    }
    

    protected Connection connection;

    private PreparedStatement netGetStmt;

    private PreparedStatement netPutStmt;

    private PreparedStatement stationGetStmt;

    private PreparedStatement netIdStmt;

    private PreparedStatement stationPutStmt;
    
    private PreparedStatement stationIdStmt;
       
    private PreparedStatement sitePutStmt;

    private PreparedStatement siteIdStmt;

    private PreparedStatement channelPutStmt;

    private PreparedStatement channelIdStmt;

    private PreparedStatement getNetStmt;

    private PreparedStatement getStationStmt;
    
    private PreparedStatement getSiteStmt;

    private PreparedStatement getChannelStmt;

    private PreparedStatement networkIdsStmt;
    
    private PreparedStatement channelIdsStmt;
    
    private PreparedStatement stationIdsStmt;

    private PreparedStatement siteIdsStmt;

    private String deleteStmt;

    static Category logger = 
        Category.getInstance(AbstractNetworkDatabase.class.getName());
}// AbstractNetworkDatabase
