package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;

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
                    (EventStationSubsetter) SodUtil.load((Element)node, "eventStation");
                break;
            }
        }
    }

    public StringTree accept(CacheEvent o, ChannelImpl channel, CookieJar cookieJar)
        throws Exception {
        StringTree wrapped = eventStationSubsetter.accept(o, (StationImpl)channel.getSite().getStation(), cookieJar);
        return new StringTreeBranch(this, wrapped.isSuccess(), wrapped);
    }

    EventStationSubsetter eventStationSubsetter;

}// EmbeddedEventStation
