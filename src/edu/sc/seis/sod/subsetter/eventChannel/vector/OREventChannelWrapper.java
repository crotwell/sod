/**
 * OREventChannelWrapper.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;

public class OREventChannelWrapper implements EventVectorSubsetter {

    public OREventChannelWrapper(EventChannelSubsetter subsetter) {
        this.subsetter = subsetter;
    }

    public OREventChannelWrapper(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                subsetter = (EventChannelSubsetter)SodUtil.load((Element)node,
                                                                "eventChannel");
                break;
            }
        }
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          CookieJar cookieJar) throws Exception {
        for(int i = 0; i < channelGroup.getChannels().length; i++) {
            if(subsetter.accept(event, channelGroup.getChannels()[i], cookieJar)) { return true; }
        }
        return false;
    }

    EventChannelSubsetter subsetter;
}