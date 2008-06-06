package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

public class EmbeddedStationSubsetter implements EventStationSubsetter {

    public EmbeddedStationSubsetter(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                stationSubsetter = (StationSubsetter)SodUtil.load((Element)node,
                                                                  "station");
                break;
            }
        }
    }

    public StringTree accept(CacheEvent eventAccess,
                             StationImpl station,
                             CookieJar cookieJar) throws Exception {
        NetworkAccess network = Start.getNetworkArm()
                .getNetwork(station.get_id().network_id);
        StringTree out = stationSubsetter.accept(station,
                                                 network);
        return new StringTreeBranch(this, out.isSuccess(), out);
    }

    private StationSubsetter stationSubsetter = null;
    
}// EmbeddedStationSubsetter
