package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfNetwork.*;

/**
 * NetworkDatabase.java
 *
 *
 * Created: Tue Oct  8 15:11:27 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface  NetworkDatabase {
    
    public int putNetwork(String serverName,
			  String serverDNS,
			  NetworkAccess networkAccess);

    public int putStation(NetworkDbObject networkDbObject,
			  Station station);

    public int putSite(StationDbObject stationDbObject,
		       Site site);
    
    public int putChannel(SiteDbObject siteDbObject,
			  Channel channel);
    
    public int getNetworkDbId(int stationdbid);
    
    public int getStationDbId(int sitedbid);

    public int getSiteDbId(int channeldbid);

    public int getNetworkDbId(NetworkAccess networkAccess);
    
    public int getStationDbId(NetworkDbObject networkDbObject,
			      Station station);

    public int getSiteDbId(StationDbObject stationDbObject,
			   Site site);

    public int getChannelDbId(SiteDbObject siteDbObject,
			      Channel channel);

    public NetworkAccess getNetworkAccess(int networkid);

    public Station getStation(int stationid);
 
    public Site getSite(int siteid);

    public Channel getChannel(int channelid);

    public NetworkId getNetworkId(int networkid);

    public StationId getStationId(int stationid);

    public SiteId getSiteId(int siteid);

    public ChannelId getChannelId(int channelid);

    public NetworkDbObject[] getNetworks();

    public StationDbObject[] getStations(int networkid);

    public SiteDbObject[] getSites(int stationid);

    public ChannelDbObject[] getChannels(int siteid);
  
    public void setTime(String serverName, String serverDNS, edu.iris.Fissures.Time time);
    
    public edu.iris.Fissures.Time getTime(String serverName, String serverDNS);
    
    public void incrementTime(String serverName, String serverDNS, int numDays);

    public void clean();
    
}// NetworkDatabase
