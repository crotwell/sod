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
   //  public void put(String serverName,
// 		    String serverDNS,
// 		    String network_code,
// 		    String station_code,
// 		    String site_code,
// 		    String channel_code,
// 		    edu.iris.Fissures.Time network_time,
// 		    edu.iris.Fissures.Time channel_time,
// 		    String channelIdIOR);

    public int put(String serverName,
		    String serverDNS,
		    Channel channel);
    

    public int getId(String serverName,
		     String serverDNS,
		     Channel channel);

//     public int get(String serverName,
// 		   String serverDNS,
// 		   String network_code,
// 		   String station_code,
// 		   String site_code,
// 		   String channel_code,
// 		   edu.iris.Fissures.Time network_time,
// 		   edu.iris.Fissures.Time channel_time);
    
    
    
}// NetworkDatabase
