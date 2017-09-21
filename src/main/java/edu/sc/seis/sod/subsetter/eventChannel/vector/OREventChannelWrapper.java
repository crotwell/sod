/**
 * OREventChannelWrapper.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
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

    public StringTree accept(CacheEvent event,
                             ChannelGroup channelGroup,
                             MeasurementStorage cookieJar) throws Exception {
        StringTree[] result = new StringTree[channelGroup.getChannels().length];
        int i;
        for(i = 0; i < channelGroup.getChannels().length; i++) {
            result[i] = subsetter.accept(event,
                                         channelGroup.getChannels()[i],
                                         cookieJar);
            if(result[i].isSuccess()) {
                return new StringTreeBranch(this, true, result);
            }
        }
        return new StringTreeBranch(this, false, result);
    }

    EventChannelSubsetter subsetter;
}