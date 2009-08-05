/**
 * ORRequestSubsetterWrapper.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request.vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.request.Request;

public class ORRequestWrapper implements VectorRequest {

    public ORRequestWrapper(Request subsetter) {
        this.subsetter = subsetter;
    }

    public ORRequestWrapper(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                subsetter = (Request)SodUtil.load((Element)node, "request");
                break;
            }
        }
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        StringTree[] result = new StringTree[channelGroup.getChannels().length];
        for(int i = 0; i < channelGroup.getChannels().length; i++) {
            result[i] = subsetter.accept(event,
                                channelGroup.getChannels()[i],
                                request[i],
                                cookieJar);
            if(result[i].isSuccess()) { return new StringTreeBranch(this, true, result); }
        }
        return new StringTreeBranch(this, true, result);
    }

    Request subsetter;
}