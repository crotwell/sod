/**
 * ANDEventChannelWrapper.java
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
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;

public class ANDEventChannelWrapper implements EventVectorSubsetter {

    public ANDEventChannelWrapper(EventChannelSubsetter subsetter) {
        this.subsetter = subsetter;
    }

    public ANDEventChannelWrapper(Element config) throws ConfigurationException {
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

    public StringTree accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          CookieJar cookieJar) throws Exception {
        StringTree[] results = new StringTree[channelGroup.getChannels().length];
        int i;
        for(i = 0; i < channelGroup.getChannels().length; i++) {
            results[i] = subsetter.accept(event,
                                          channelGroup.getChannels()[i],
                                          cookieJar);
            if(! results[i].isSuccess()) { break; }
        }
        return new StringTreeBranch(this, results[i].isSuccess(), results);
    }

    EventChannelSubsetter subsetter;
}