package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import java.io.*;
import org.w3c.dom.*;

/**
 * NullNetworkStatusProcessor.java
 *
 * Does nothing except define empty methods.
 *
 * Created: Tue Mar 18 14:29:38 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NullNetworkStatusProcessor {

    public NullNetworkStatusProcessor() {

    }

    public NullNetworkStatusProcessor(Element config) {

    }
    
    public void networkId(boolean success,
                          NetworkId networkId, 
                          CookieJar cookies) {
    }

    public void networkAttr(boolean success,
                            NetworkAttr networkAttr,
                            CookieJar cookies) {
    }

    public void stationId(boolean success,
                          StationId id,
                          CookieJar cookies) {
    }

    public void station(boolean success,
                        NetworkAccess network,
                        Station station,
                        CookieJar cookies) {
    }

    public void siteId(boolean success,
                       SiteId id,
                       CookieJar cookies) {
    }

    public void site(boolean success,
                     NetworkAccess network, 
                     Site site, 
                     CookieJar cookies) {
    }

    public void channelId(boolean success,
                          ChannelId id,
                          CookieJar cookies) {
    }

    public void channel(boolean success,
                        NetworkAccess network, 
                        Channel channel, 
                        CookieJar cookies) {
    }

}
