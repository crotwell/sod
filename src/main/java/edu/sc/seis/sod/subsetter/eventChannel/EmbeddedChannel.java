package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;

/**
 * EmbeddedEventStation.java Created: Wed Oct 30 11:54:58 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public class EmbeddedChannel implements EventChannelSubsetter {

    public EmbeddedChannel(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                channelSubsetter = (ChannelSubsetter)SodUtil.load((Element)node,
                                                                  "channel");
                break;
            }
        }
    }

    public StringTree accept(CacheEvent o,
                             ChannelImpl channel,
                             CookieJar cookieJar) throws Exception {
        ProxyNetworkAccess network = Start.getNetworkArm()
                .getNetwork(channel.get_id().network_id);
        StringTree result = channelSubsetter.accept(channel, network);
        return new StringTreeBranch(this, result.isSuccess(), result);
    }

    ChannelSubsetter channelSubsetter;
}// EmbeddedChannel