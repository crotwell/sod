package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    public StringTree accept(EventAccessOperations eventAccess,
                             Station station,
                             CookieJar cookieJar) throws Exception {
        NetworkAccess network = Start.getNetworkArm()
                .getNetwork(station.get_id().network_id);
        return stationSubsetter.accept(station,
                                       network);
    }

    private StationSubsetter stationSubsetter = null;
}// EmbeddedStationSubsetter
