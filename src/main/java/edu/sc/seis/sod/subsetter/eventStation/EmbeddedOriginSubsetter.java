package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;

public class EmbeddedOriginSubsetter implements EventStationSubsetter {

    public EmbeddedOriginSubsetter(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                originSubsetter = (OriginSubsetter)SodUtil.load((Element)node,
                                                                "origin");
                nodeName = ((Element)node).getTagName();
                break;
            }
        }
    }

    public StringTree accept(CacheEvent eventAccess,
                             Station station,
                          MeasurementStorage cookieJar) throws Exception {
        StringTree result = originSubsetter.accept(eventAccess,
                                                   (EventAttrImpl)eventAccess.get_attributes(),
                                                   (OriginImpl)eventAccess.get_preferred_origin());
        return new StringTreeBranch(this, result.isSuccess(), result);
    }

    private String nodeName;
    
    private OriginSubsetter originSubsetter = null;
    
}// EmbeddedOriginSubsetter
