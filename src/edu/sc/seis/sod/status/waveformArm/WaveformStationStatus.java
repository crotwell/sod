/**
 * WaveformStationStatus.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import java.util.HashMap;
import java.util.HashSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class WaveformStationStatus implements WaveformArmMonitor, NetworkArmMonitor {

    public WaveformStationStatus(Element config) {
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Element element = (Element)n;
                if (element.getTagName().equals("fileDir")){
                    fileDir = SodUtil.getNestedText(element);
                } else if(n.getNodeName().equals("networkTemplate")) {
                    networkTemplate = SodUtil.getNestedText(element);
                }
            }
        }
        if (fileDir == null){
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
        if(Start.getNetworkArm() != null) Start.getNetworkArm().add(this);
    }

    public void update(EventChannelPair ecp) {

    }

    public void setArmStatus(String status) throws Exception {
        // TODO
    }

    public void change(Station station, Status s) {
        synchronized(stationMap) {
            stationMap.put(station, s);
        }
    }

    public void change(Channel channel, Status s) {
        // TODO
    }

    public void change(NetworkAccess net, Status status){
        synchronized(networkMap){
            networkMap.put(net, status);
        }
    }

    public void change(Site site, Status s) {
        // TODO
    }


    private HashMap networkMap = new HashMap();

    private HashMap stationMap = new HashMap();

    private String fileDir;

    private String networkTemplate;

}

