/**
 * ORAvailableDataWrapper.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;

public class ORAvailableDataWrapper implements VectorAvailableDataSubsetter {

    public ORAvailableDataWrapper(AvailableDataSubsetter subsetter) {
        this.subsetter = subsetter;
    }

    public ORAvailableDataWrapper(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                subsetter = (AvailableDataSubsetter)SodUtil.load((Element)node,
                                                                 "availableData");
                break;
            }
        }
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          CookieJar cookieJar) throws Exception {
        for(int i = 0; i < channelGroup.getChannels().length; i++) {
            if(subsetter.accept(event,
                                channelGroup.getChannels()[i],
                                original[i],
                                available[i],
                                cookieJar)) { return true; }
        }
        return false;
    }

    AvailableDataSubsetter subsetter;
}