/**
 * NetworkTemplateGenerator.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.NetworkStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.NetworkFormatter;
import edu.sc.seis.sod.subsetter.SiteFormatter;
import edu.sc.seis.sod.subsetter.StationFormatter;
import edu.sc.seis.sod.subsetter.TemplateFileLoader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NetworkInfoTemplateGenerator implements NetworkStatus {
    
    private String fileDir, netsOutputFileName, stasOutputFileName, sitesOutputFileName, chansOutputFileName;
    private NetworkFormatter netFormatter;
    private StationFormatter staFormatter;
    private SiteFormatter siteFormatter;
    private NetworkStatusTemplate netTemplate;
    private HashMap stationTemplates = new HashMap(); //station templates by network
    private HashMap siteTemplates = new HashMap(); //site templates by station
    private HashMap channelTemplates = new HashMap(); //channel templates by site
    private Logger logger = Logger.getLogger(NetworkInfoTemplateGenerator.class);
    private Element netConfig, staConfig, siteConfig, chanConfig;
    
    public NetworkInfoTemplateGenerator(Element el) throws Exception{
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("fileDir")){
                fileDir = n.getFirstChild().getNodeValue();
            }
            else if (n.getNodeName().equals("netsOutputFileName")){
                netsOutputFileName = n.getFirstChild().getNodeValue();
            }
            else if (n.getNodeName().equals("netDir")){
                netFormatter = new NetworkFormatter((Element)n);
            }
            else if (n.getNodeName().equals("stationsOutputFileName")){
                stasOutputFileName = n.getFirstChild().getNodeValue();
            }
            else if (n.getNodeName().equals("stationDir")){
                staFormatter = new StationFormatter((Element)n);
            }
            else if (n.getNodeName().equals("sitesOutputFileName")){
                sitesOutputFileName = n.getFirstChild().getNodeValue();
            }
            else if (n.getNodeName().equals("siteDir")){
                siteFormatter = new SiteFormatter((Element)n);
            }
            else if (n.getNodeName().equals("channelsOutputFileName")){
                chansOutputFileName = n.getFirstChild().getNodeValue();
            }
            else if (n.getNodeName().equals("netConfig")){
                netConfig = TemplateFileLoader.getTemplate((Element)n);
            }
            else if (n.getNodeName().equals("stationConfig")){
                staConfig = TemplateFileLoader.getTemplate((Element)n);
            }
            else if (n.getNodeName().equals("siteConfig")){
                siteConfig = TemplateFileLoader.getTemplate((Element)n);
            }
            else if (n.getNodeName().equals("channelConfig")){
                chanConfig = TemplateFileLoader.getTemplate((Element)n);
            }
        }
        if (fileDir == null || netFormatter == null || staFormatter == null || siteFormatter == null
            || netConfig == null || staConfig == null || siteConfig == null || chanConfig == null
            || netsOutputFileName == null || stasOutputFileName == null || sitesOutputFileName == null
            || chansOutputFileName == null){
            throw new IllegalArgumentException(
                "The configuration element must contain a fileDir, netsOutputFileName, netDir, stationsOutputFileName, stationDir, sitesOutputFileName,"
                    + " siteDir, channelsOutputFileName, netConfig, stationConfig, siteConfig, and chanConfig.");
        }
        
        netTemplate = new NetworkStatusTemplate(netConfig, fileDir + '/' + netsOutputFileName);
    }
    
    public void change(NetworkAccess net, RunStatus status){
        netTemplate.change(net, status);
        getStationsInNetworkTemplate(net);
    }
    
    public void change(Station station, RunStatus status){
        logger.debug("change(station, status): " + station.get_code() + ", " + status.toString());
        StationsInNetworkTemplate snt = getStationsInNetworkTemplate(station);
        snt.change(station, status);
        getSitesInStationTemplate(station);
    }
    
    public void change(Site site, RunStatus status) {
        logger.debug("change(site, status): " + SiteFormatter.formatSiteCode(site.get_code()) + ", " + status.toString());
        SitesInStationTemplate sst = getSitesInStationTemplate(site);
        sst.change(site, status);
        getChannelsInSiteTemplate(site);
    }
    
    public void change(Channel channel, RunStatus status) {
        logger.debug("change(channel, status): " + channel.get_code() + ", " + status.toString());
        ChannelsInSiteTemplate cst = getChannelsInSiteTemplate(channel);
        cst.change(channel, status);
    }
    
    public NetworkStatusTemplate getNetworkStatusTemplate(){
        return netTemplate;
    }
    
    public StationsInNetworkTemplate getStationsInNetworkTemplate(NetworkAccess net){
        if (!contains(net)){
            try {
                stationTemplates.put(net.get_attributes().name + net.get_attributes().get_id().begin_time.date_time,
                                     new StationsInNetworkTemplate(staConfig,
                                                                   fileDir
                                                                       + '/'
                                                                       + netFormatter.getResult(net)
                                                                       + '/'
                                                                       + stasOutputFileName,
                                                                   net));
            } catch (IOException e) {
                CommonAccess.handleException(e, "trouble creating StationsInNetworkTemplate");
            }
        }
        return (StationsInNetworkTemplate)stationTemplates.get(net.get_attributes().name
                                                                   + net.get_attributes().get_id().begin_time.date_time);
    }
    
    public StationsInNetworkTemplate getStationsInNetworkTemplate(Station station){
        return getStationsInNetworkTemplate(getNetworkFromStation(station));
    }
    
    public SitesInStationTemplate getSitesInStationTemplate(Station station){
        if (!contains(station)){
            try {
                siteTemplates.put(station.name
                                      + station.get_id().begin_time.date_time,
                                  new SitesInStationTemplate(siteConfig,
                                                             fileDir
                                                                 + '/'
                                                                 + netFormatter.getResult(getNetworkFromStation(station))
                                                                 + '/'
                                                                 + staFormatter.getResult(station)
                                                                 + '/'
                                                                 + sitesOutputFileName,
                                                             station));
            } catch (IOException e) {
                CommonAccess.handleException(e, "trouble creating SitesInStationTemplate");
            }
        }
        return (SitesInStationTemplate)siteTemplates.get(station.name
                                                             + station.get_id().begin_time.date_time);
    }
    
    public SitesInStationTemplate getSitesInStationTemplate(Site site){
        return getSitesInStationTemplate(site.my_station);
    }
    
    public ChannelsInSiteTemplate getChannelsInSiteTemplate(Site site){
        if (!contains(site)){
            try {
                channelTemplates.put(site.my_station.my_network.get_code()
                                         + site.my_station.get_code() + SiteFormatter.formatSiteCode(site.get_code())
                                         + site.get_id().begin_time.date_time,
                                     new ChannelsInSiteTemplate(chanConfig,
                                                                fileDir
                                                                    + '/'
                                                                    + netFormatter.getResult(getNetworkFromStation(site.my_station))
                                                                    + '/'
                                                                    + staFormatter.getResult(site.my_station)
                                                                    + '/'
                                                                    + siteFormatter.getResult(site)
                                                                    + '/'
                                                                    + chansOutputFileName,
                                                                site));
            } catch (IOException e) {
                CommonAccess.handleException(e, "trouble creating ChannelsInSiteTemplate");
            }
        }
        return (ChannelsInSiteTemplate)channelTemplates.get(site.my_station.my_network.get_code()
                                                                + site.my_station.get_code()
                                                                + SiteFormatter.formatSiteCode(site.get_code())
                                                                + site.get_id().begin_time.date_time);
    }
    
    public ChannelsInSiteTemplate getChannelsInSiteTemplate(Channel chan){
        return getChannelsInSiteTemplate(chan.my_site);
    }
    
    public NetworkAccess getNetworkFromStation(Station station){
        NetworkAccess staNet = null;
        Iterator it = stationTemplates.keySet().iterator();
        while (it.hasNext() && staNet == null){
            String cur = (String)it.next();
            if (cur.equals(station.my_network.name + station.my_network.get_id().begin_time.date_time)){
                staNet = ((StationsInNetworkTemplate)stationTemplates.get(cur)).getNetwork();
                logger.debug("found station "
                                 + station.get_code()
                                 + "'s network ("
                                 + staNet.get_attributes().get_code()
                                 +") in stationTemplates");
            }
        }
        return staNet;
    }
    
    public boolean contains(NetworkAccess net){
        return stationTemplates.containsKey(net.get_attributes().name
                                                + net.get_attributes().get_id().begin_time.date_time);
    }
    
    public boolean contains(Station sta){
        return siteTemplates.containsKey(sta.name + sta.get_id().begin_time.date_time);
    }
    
    public boolean contains(Site site){
        return channelTemplates.containsKey(site.my_station.my_network.get_code()
                                                + site.my_station.get_code()
                                                + SiteFormatter.formatSiteCode(site.get_code())
                                                + site.get_id().begin_time.date_time);
    }
    
    public void setArmStatus(String status) { netTemplate.setArmStatus(status); }
    
}


