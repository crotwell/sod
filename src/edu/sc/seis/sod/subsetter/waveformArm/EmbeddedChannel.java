package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.ChannelSubsetter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveFormArm.EventChannelSubsetter;
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

    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations o,
              NetworkAccess network,
              Channel channel,
              CookieJar cookies)
    throws Exception
    {
    return channelSubsetter.accept(network,
                       channel,
                       cookies);
    }

    ChannelSubsetter channelSubsetter;

}// EmbeddedChannel
