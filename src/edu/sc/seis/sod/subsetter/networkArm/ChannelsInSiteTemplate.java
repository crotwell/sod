/**
 * ChannelsInSiteTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.networkArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.ChannelGroupTemplate;
import edu.sc.seis.sod.subsetter.GenericTemplate;
import edu.sc.seis.sod.subsetter.SiteFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;



public class ChannelsInSiteTemplate extends NetworkInfoTemplate{
    
    private Site site;
    private List channelListeners = new ArrayList();
    private Logger logger = Logger.getLogger(ChannelsInSiteTemplate.class);
    
    public ChannelsInSiteTemplate(Element el, String outputLocation, Site site){
        super(outputLocation);
        this.site = site;
        parse(el);
        write();
    }
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        if (tag.equals("channels")){
            ChannelGroupTemplate cgt = new ChannelGroupTemplate(el);
            channelListeners.add(cgt);
            return cgt;
        }
        else if (tag.equals("site")){
            return new MySiteTemplate(el);
        }
        return super.getTemplate(tag,el);
    }
    
    public void change(Channel channel, RunStatus status){
        logger.debug("change(channel, status): " + site.my_station.my_network.get_code() + "."
                    + site.my_station.my_network.get_code() + "." + site.my_station.get_code()
                    + "." + site.get_code() + "." + channel.get_code() + ", " + status.toString());
        Iterator it = channelListeners.iterator();
        while (it.hasNext()){
            ((ChannelGroupTemplate)it.next()).change(channel, status);
        }
        write();
    }
    
    private class MySiteTemplate implements GenericTemplate{
        
        public MySiteTemplate(Element el){ formatter = new SiteFormatter(el); }
        
        public String getResult(){
            return formatter.getResult(site);
        }
        
        SiteFormatter formatter;
    }
}

