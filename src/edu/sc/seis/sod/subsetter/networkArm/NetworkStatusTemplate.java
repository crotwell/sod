/**
 * NetworkStatusTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.NetworkStatus;
import edu.sc.seis.sod.RunStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class NetworkStatusTemplate extends FileWritingTemplate implements NetworkStatus{
    
    private String status = "";
    private List stationListeners = new ArrayList();
    private List siteListeners = new ArrayList();
    private List channelListeners = new ArrayList();
    private List networkListeners = new ArrayList();
    private Logger logger = Logger.getLogger(NetworkStatusTemplate.class);
    
    public NetworkStatusTemplate(Element el) throws IOException{
        super(el.getAttribute("outputLocation"));
        parse(TemplateFileLoader.getTemplate(el));
    }
    
    public void change(Station station, RunStatus status) {
        logger.debug("change(Station, Status): " + station.get_code() + ", " + status.toString());
        Iterator it = stationListeners.iterator();
        while (it.hasNext()){
            StationGroupTemplate sgt = (StationGroupTemplate)it.next();
            sgt.change(station, status);
        }
        write();
    }
    
    public void change(Site site, RunStatus status) {
        logger.debug("change(Site, Status): " + site.get_code() + ", " + status.toString());
        Iterator it = siteListeners.iterator();
        while (it.hasNext()){
            SiteGroupTemplate sgt = (SiteGroupTemplate)it.next();
            sgt.change(site, status);
        }
        write();
    }
    
    public void change(Channel channel, RunStatus status) {
        logger.debug("change(Channel, Status): " + channel.get_code()
                         + ", " + status.toString());
        Iterator it = channelListeners.iterator();
        while (it.hasNext()){
            ChannelGroupTemplate cgt = (ChannelGroupTemplate)it.next();
            cgt.change(channel, status);
        }
        write();
    }
    
    public void change(NetworkAccess networkAccess, RunStatus status) {
        logger.debug("change(Network, Status): " + networkAccess.get_attributes().name
                         + ", " + status.toString());
        Iterator it = networkListeners.iterator();
        while (it.hasNext()){
            NetworkGroupTemplate ngt = (NetworkGroupTemplate)it.next();
            ngt.change(networkAccess, status);
        }
        write();
    }
    
    public void setArmStatus(String status) {
        logger.debug("setArmStatus: " + status);
        this.status = status;
        write();
    }

    private class StatusFormatter implements GenericTemplate{
        public String getResult(){ return status; }
    }
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        Template t = null;
        if (tag.equals("channels")) {
            t = new ChannelGroupTemplate(el);
            channelListeners.add(t);
        }
        else if (tag.equals("sites")) {
            t = new SiteGroupTemplate(el);
            siteListeners.add(t);
        }
        else if (tag.equals("stations")) {
            t = new StationGroupTemplate(el);
            stationListeners.add(t);
        }
        else if (tag.equals("networks")) {
            t = new NetworkGroupTemplate(el);
            networkListeners.add(t);
        }
        else if (tag.equals("status")){
            return new StatusFormatter();
        }
        return t;
    }
}

