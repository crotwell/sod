/**
 * ANDAvailableDataWrapper.java
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
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;

public class ANDAvailableDataWrapper implements VectorAvailableDataSubsetter {

    public ANDAvailableDataWrapper(AvailableDataSubsetter subsetter) {
        this.subsetter = subsetter;
    }

    public ANDAvailableDataWrapper(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                subsetter = (AvailableDataSubsetter)SodUtil.load((Element)node,
                                                                 "availableData");
                break;
            }
        }
    }

    public StringTree accept(EventAccessOperations event,
                             ChannelGroup channelGroup,
                             RequestFilter[][] original,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        StringTree[] result = new StringTree[channelGroup.getChannels().length];
        for(int i = 0; i < channelGroup.getChannels().length; i++) {
            result[i] = subsetter.accept(event,
                                         channelGroup.getChannels()[i],
                                         original[i],
                                         available[i],
                                         cookieJar);
            if(!result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(channelGroup.getChannels()[j]);
                }
                return new StringTreeBranch(this, false, result);
            }
        }
        return new StringTreeBranch(this, true, result);
    }

    AvailableDataSubsetter subsetter;
}