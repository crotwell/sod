package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
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
                break;
            }
        }
    }

    public boolean accept(EventAccessOperations eventAccess,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        return originSubsetter.accept(eventAccess,
                                      eventAccess.get_attributes(),
                                      eventAccess.get_preferred_origin());
    }

    private OriginSubsetter originSubsetter = null;
}// EmbeddedOriginSubsetter
