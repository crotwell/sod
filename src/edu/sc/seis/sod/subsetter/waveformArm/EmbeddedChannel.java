package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.networkArm.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.waveformArm.EventChannelSubsetter;
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

public class EmbeddedChannel  implements EventChannelSubsetter{
    public EmbeddedChannel(Element config) throws ConfigurationException{

        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                channelSubsetter =
                    (ChannelSubsetter) SodUtil.load((Element)node, "networkArm");
                break;
            }
        }
    }

    public boolean accept(EventAccessOperations o, Channel channel, CookieJar cookieJar)
        throws Exception {
        return channelSubsetter.accept(channel);
    }

    ChannelSubsetter channelSubsetter;

}// EmbeddedChannel
