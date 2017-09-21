package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
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
                             Station station,
                             MeasurementStorage cookieJar) throws Exception {
        StringTree out = stationSubsetter.accept(station,
                                                 Start.getNetworkArm().getNetworkSource());
        return new StringTreeBranch(this, out.isSuccess(), out);
    }

    private StationSubsetter stationSubsetter = null;
    
}// EmbeddedStationSubsetter
