/**
 * ChannelsInStationTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.networkArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.ChannelGroupTemplate;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.SiteGroupTemplate;
import edu.sc.seis.sod.status.StationFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class ChannelsInStationTemplate extends NetworkInfoTemplate{

    private Station station;
    private List channelListeners = new ArrayList();
    private List siteListeners = new ArrayList();
    private Logger logger = Logger.getLogger(ChannelsInStationTemplate.class);

    public ChannelsInStationTemplate(Element el, String baseDir, String outputLocation, Station sta)  throws ConfigurationException, IOException{
        super(baseDir, outputLocation);
        station = sta;
        parse(el);
        write();
    }

    protected Object getTemplate(String tag, Element el) throws ConfigurationException  {
        if (tag.equals("channels")){
            ChannelGroupTemplate cgt = new ChannelGroupTemplate(el);
            channelListeners.add(cgt);
            return cgt;
        }
        else if (tag.equals("sites")){
            SiteGroupTemplate sgt = new SiteGroupTemplate(el);
            siteListeners.add(sgt);
            return sgt;
        }
        else if (tag.equals("station")){
            return new MyStationTemplate(el);
        }
        return super.getTemplate(tag,el);
    }

    public void change(Channel channel, Status status){
        logger.debug("change(channel, status): "
                         + channel.my_site.my_station.my_network.get_code()
                         + "." + channel.my_site.my_station.get_code()
                         + "." + channel.my_site.get_code()
                         + "." + channel.get_code()
                         + ", " + status.toString());
        Iterator it = channelListeners.iterator();
        while (it.hasNext()){
            ((ChannelGroupTemplate)it.next()).change(channel, status);
        }
        write();
    }

    private class MyStationTemplate implements GenericTemplate{
        public MyStationTemplate(Element el) throws ConfigurationException  {
            formatter = new StationFormatter(el);
        }

        public String getResult(){
            return formatter.getResult(station);
        }

        StationFormatter formatter;
    }
}


