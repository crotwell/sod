package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * EmbeddedEventStation.java
 *
 *
 * Created: Wed Oct 30 11:54:58 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EmbeddedEventStation  implements EventChannelSubsetter{
    public EmbeddedEventStation(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                eventStationSubsetter =
                    (EventStationSubsetter) SodUtil.load((Element)node, "eventArm");
                break;
            }
        }
    }

    public boolean accept(EventAccessOperations o, Channel channel, CookieJar cookieJar)
        throws Exception {
        return eventStationSubsetter.accept(o, channel.my_site.my_station, cookieJar);
    }

    EventStationSubsetter eventStationSubsetter;

}// EmbeddedEventStation
