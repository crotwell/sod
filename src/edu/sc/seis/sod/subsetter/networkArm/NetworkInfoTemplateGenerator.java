/**
 * NetworkTemplateGenerator.java
 *
 * @author Created by Omnicore CodeGuide
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
import edu.sc.seis.sod.subsetter.TemplateFileLoader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NetworkInfoTemplateGenerator implements NetworkStatus {
    
    private NetworkFormatter formatter;
    private Map templatesByNet = new HashMap();
    private Map templatesByStation = new HashMap();
    private Element config;
    private Logger logger = Logger.getLogger(NetworkInfoTemplateGenerator.class);
    
    public NetworkInfoTemplateGenerator(Element el){
        config = el;
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("fileLoc")){
                formatter = new NetworkFormatter((Element)n);
            }
            else if (n.getNodeName().equals("externalConfig")){
                try {
                    config = TemplateFileLoader.getTemplate((Element)n);
                } catch (IOException e) {
                    CommonAccess.getCommonAccess().handleException(e, "trouble getting config template");
                }
            }
        }
        if (formatter == null){
            throw new IllegalArgumentException("The configuration element must contain a fileLoc and an external config");
        }
    }
    
    public void change(NetworkAccess net, RunStatus status){
        getTemplate(net);
    }
    
    public void change(Station station, RunStatus status) {
        logger.debug("change(station, status): " + station.get_code() + ", " + status.toString());
        if (!templatesByStation.containsKey(station)){
            logger.debug("templatesByStation does NOT contain Key " + station.get_code());
            NetworkAccess staNet = null;
            logger.debug("size of TemplatesByNet: " + templatesByNet.keySet().size());
            Iterator it = templatesByNet.keySet().iterator();
            while(it.hasNext() && staNet == null){
                NetworkAccess cur = (NetworkAccess)it.next();
                //tests by network's name right now
                //this may need to be changed...
                if (cur.get_attributes().name.equals(station.my_network.name)){
                    staNet = cur;
                    logger.debug("found station "
                                     + station.get_code()
                                     + "'s network ("
                                     + staNet.get_attributes().get_code()
                                     +") in templatesByNet");
                }
            }
            if (staNet != null){
                logger.debug("staNet != null");
                NetworkInfoTemplate nit = getTemplate(staNet);
                nit.change(station, status);
                templatesByStation.put(station, nit);
            }
        }
        else{
            logger.debug("templatesByStation.containsKey(" + station.get_code() + ")");
            NetworkInfoTemplate nit = (NetworkInfoTemplate)templatesByStation.get(station);
            nit.change(station, status);
        }
    }
    
    public NetworkInfoTemplate getTemplate(NetworkAccess net){
        if (!templatesByNet.containsKey(net)){
            templatesByNet.put(net, new NetworkInfoTemplate(config, formatter.getResult(net), net));
        }
        return (NetworkInfoTemplate)templatesByNet.get(net);
    }
    
    public boolean contains(NetworkAccess net){
        return templatesByNet.containsKey(net);
    }
    
    public void setArmStatus(String status) {}
    
    public void change(Site site, RunStatus status) {}
    
    public void change(Channel channel, RunStatus status) {}
    
    
    
    
    
    
}

