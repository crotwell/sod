/**
 * NetworkTemplateGenerator.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.networkArm;
import edu.sc.seis.sod.status.networkArm.*;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.status.networkArm.NetworkStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.NetworkFormatter;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.TemplateFileLoader;
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
    private NetworkStatusTemplate netTemplate;
    private HashMap stationTemplates = new HashMap(); //station templates by network id string
    private HashMap channelTemplates = new HashMap(); //channel templates by station id string
    private Logger logger = Logger.getLogger(NetworkInfoTemplateGenerator.class);
    private Element netConfig, staConfig, siteConfig, chanConfig;
    
    public NetworkInfoTemplateGenerator(Element el) throws Exception{
		NodeList nl = el.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals("fileDir")){
				fileDir = n.getFirstChild().getNodeValue();
			}
			else if (n.getNodeName().equals("netConfig")){
				netConfig = TemplateFileLoader.getTemplate((Element)n);
				
				Node tmpEl = SodUtil.getElement(netConfig, "filename");
				netsOutputFileName = tmpEl.getFirstChild().getNodeValue();
				logger.debug("netsOutputFileName = " + netsOutputFileName);
				netConfig.removeChild(tmpEl);
			}
			else if (n.getNodeName().equals("stationConfig")){
				staConfig = TemplateFileLoader.getTemplate((Element)n);
				
				Node tmpEl = SodUtil.getElement(staConfig, "outputLocation");
				netFormatter = new NetworkFormatter((Element)tmpEl);
				staConfig.removeChild(tmpEl);
				
				tmpEl = SodUtil.getElement(staConfig, "filename");
				stasOutputFileName = tmpEl.getFirstChild().getNodeValue();
				logger.debug("stasOutputFileName = " + stasOutputFileName);
				staConfig.removeChild(tmpEl);
			}
			else if (n.getNodeName().equals("channelConfig")){
				chanConfig = TemplateFileLoader.getTemplate((Element)n);
				
				Node tmpEl = SodUtil.getElement(chanConfig, "outputLocation");
				staFormatter = new StationFormatter((Element)tmpEl);
				chanConfig.removeChild(tmpEl);
				
				tmpEl = SodUtil.getElement(chanConfig, "filename");
				chansOutputFileName = tmpEl.getFirstChild().getNodeValue();
				logger.debug("chansOutputFileName = " + chansOutputFileName);
				chanConfig.removeChild(tmpEl);
			}
		}
		if (fileDir == null || netFormatter == null || staFormatter == null || netConfig == null
			|| staConfig == null || chanConfig == null || netsOutputFileName == null
			|| stasOutputFileName == null || chansOutputFileName == null){
			throw new IllegalArgumentException(
											   "The configuration element must contain a fileDir, netConfig, stationConfig, and chanConfig.");
		}
		
		netTemplate = new NetworkStatusTemplate(netConfig, fileDir + '/' + netsOutputFileName);
    }
    
    public void change(NetworkAccess net, RunStatus status) throws IOException{
		netTemplate.change(net, status);
		getStationsInNetworkTemplate(net);
    }
    
    public void change(Station station, RunStatus status) throws IOException {
		logger.debug("change(station, status): " + station.get_code() + ", " + status.toString());
		StationsInNetworkTemplate snt = getStationsInNetworkTemplate(station);
		snt.change(station, status);
		getChannelsInStationTemplate(station);
    }
    
    public void change(Channel channel, RunStatus status) throws IOException {
		logger.debug("change(channel, status): " + channel.get_code() + ", " + status.toString());
		ChannelsInStationTemplate cst = getChannelsInStationTemplate(channel);
		cst.change(channel, status);
    }
    
    public NetworkStatusTemplate getNetworkStatusTemplate(){
		return netTemplate;
    }
    
    public StationsInNetworkTemplate getStationsInNetworkTemplate(NetworkAccess net){
		if (!contains(net)){
			try {
				stationTemplates.put(getIDString(net),
									 new StationsInNetworkTemplate(staConfig,
																   fileDir
																	   + '/'
																	   + netFormatter.getResult(net)
																	   + '/'
																	   + stasOutputFileName,
																   net));
				logger.debug("successfully put " + getIDString(net) + "'s StationsInNetworkTemplate in map");
			} catch (IOException e) {
				CommonAccess.handleException(e, "trouble creating StationsInNetworkTemplate");
			}
		}
		StationsInNetworkTemplate snt = (StationsInNetworkTemplate)stationTemplates.get(getIDString(net));
		return snt;
    }
    
    public StationsInNetworkTemplate getStationsInNetworkTemplate(Station station){
		return getStationsInNetworkTemplate(getNetworkFromStation(station));
    }
    
    public ChannelsInStationTemplate getChannelsInStationTemplate(Station station){
		if (!contains(station)){
			try {
				channelTemplates.put(getIDString(station),
									 new ChannelsInStationTemplate(chanConfig,
																   fileDir
																	   + '/'
																	   + netFormatter.getResult(getNetworkFromStation(station))
																	   + '/'
																	   + staFormatter.getResult(station)
																	   + '/'
																	   + chansOutputFileName,
																   station));
			} catch (IOException e) {
				CommonAccess.handleException(e, "trouble creating ChannelsInSiteTemplate");
			}
		}
		ChannelsInStationTemplate cst = (ChannelsInStationTemplate)channelTemplates.get(getIDString(station));
		return cst;
    }
    
    public ChannelsInStationTemplate getChannelsInStationTemplate(Channel chan){
		return getChannelsInStationTemplate(chan.my_site.my_station);
    }
    
    public NetworkAccess getNetworkFromStation(Station station){
		NetworkAccess staNet = null;
		Iterator it = stationTemplates.keySet().iterator();
		while (it.hasNext() && staNet == null){
			String cur = (String)it.next();
			if (cur.equals(getIDString(station.my_network))){
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
		return stationTemplates.containsKey(getIDString(net));
    }
    
    public boolean contains(Station sta){
		return channelTemplates.containsKey(getIDString(sta));
    }
    
    private String getIDString(NetworkAccess net){
		return getIDString(net.get_attributes());
    }
    
    private String getIDString(NetworkAttr netAttr){
		return netAttr.name + '.'
			+ netAttr.get_id().begin_time.date_time + '-'
			+ netAttr.effective_time.end_time.date_time;
    }
    
    private String getIDString(Station sta){
		return sta.name + '.' + sta.get_id().begin_time.date_time + '-'
			+ sta.effective_time.end_time.date_time;
    }
    
    public void setArmStatus(String status)  throws IOException {
		netTemplate.setArmStatus(status);
    }
	
    public void change(Site site, RunStatus status) throws Exception {
		// noImpl
    }
}



