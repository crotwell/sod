package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * NetworkStatusProcessor.java
 *
 *
 * Created: Tue Mar 18 14:11:29 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */

public interface NetworkStatusProcessor {
    
    public void networkId(boolean success,
                          NetworkId networkId, 
                          CookieJar cookies);

    public void networkAttr(boolean success,
                            NetworkAttr attr,
                            CookieJar cookies);

    public void stationId(boolean success,
                          StationId id,
                          CookieJar cookies);

    public void station(boolean success,
                        NetworkAccess network,
                        Station station,
                        CookieJar cookies);

    public void siteId(boolean success,
                       SiteId id,
                       CookieJar cookies);

    public void site(boolean success,
                     NetworkAccess network, 
                     Site site, 
                     CookieJar cookies);

    public void channelId(boolean success,
                          ChannelId id,
                          CookieJar cookies);

    public void channel(boolean success,
                        NetworkAccess network, 
                        Channel channel, 
                        CookieJar cookies);
                          
}// NetworkStatusProcessor
