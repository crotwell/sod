package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;

/**
 * EmbeddedEventStation.java Created: Wed Oct 30 11:54:58 2002
 * 
 * @author Philip Crotwell 
 */
public class EmbeddedChannel implements EventChannelSubsetter {

    public EmbeddedChannel(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                channelSubsetter = (ChannelSubsetter)SodUtil.load((Element)node,
                                                                  new String[] {"channel",
                                                                                "station",
                                                                                "network"});
                break;
            }
        }
    }

    public StringTree accept(CacheEvent o,
                             ChannelImpl channel,
                             CookieJar cookieJar) throws Exception {
        StringTree result = channelSubsetter.accept(channel, Start.getNetworkArm().getNetworkSource());
        return new StringTreeBranch(this, result.isSuccess(), result);
    }

    ChannelSubsetter channelSubsetter;
}// EmbeddedChannel
