package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import java.io.*;
import org.w3c.dom.*;

/**
 * PrintlineNetworkStatus.java
 *
 *
 * Created: Tue Mar 18 14:29:38 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class PrintlineNetworkStatus {
    public PrintlineNetworkStatus(Element config) {
        filename = SodUtil.getNestedText(config);
    } // PrintlineNetworkStatus constructor
    
    public void networkId(boolean success,
                          NetworkId networkId, 
                          CookieJar cookies) {
        print(success, "networkId - "+NetworkIdUtil.toString(networkId));
    }

    public void networkAttr(boolean success,
                            NetworkAttr networkAttr,
                            CookieJar cookies) {
        print(success, "networkAttr - "+NetworkIdUtil.toString(networkAttr.get_id()));
    }

    public void stationId(boolean success,
                          StationId id,
                          CookieJar cookies) {
        print(success, "stationId - "+StationIdUtil.toString(id));
    }

    public void station(boolean success,
                        NetworkAccess network,
                        Station station,
                        CookieJar cookies) {
        print(success, "station - "+StationIdUtil.toString(station.get_id()));
    }

    public void siteId(boolean success,
                       SiteId id,
                       CookieJar cookies) {
        print(success, "siteId - "+SiteIdUtil.toString(id));
    }

    public void site(boolean success,
                     NetworkAccess network, 
                     Site site, 
                     CookieJar cookies) {
        print(success, "site - "+SiteIdUtil.toString(site.get_id()));
    }

    public void channelId(boolean success,
                          ChannelId id,
                          CookieJar cookies) {
        print(success, "channelId - "+ChannelIdUtil.toString(id));
    }

    public void channel(boolean success,
                        NetworkAccess network, 
                        Channel channel, 
                        CookieJar cookies) {
        print(success, "channel - "+ChannelIdUtil.toString(channel.get_id()));
    }

    void print(boolean success, String msg) {
        try {
            if ( bwriter == null) {
                if (filename != null && filename.length() != 0) {
                    FileWriter fwriter = new FileWriter(filename, true);
                    bwriter = new BufferedWriter(fwriter);
                } else {
                    bwriter = new BufferedWriter(new OutputStreamWriter(System.out));
                } // end of else
            } // end of if ()

            if ( success ) {
                bwriter.write("Success: ");
            } else {
                bwriter.write("FAIL   : ");
            } // end of if ()
            bwriter.write(msg);
            bwriter.newLine();
            bwriter.flush();
        } catch(Exception e) {
            
        }
        
    }
    
    BufferedWriter bwriter = null;
    String filename = null;

} // PrintlineNetworkStatus
