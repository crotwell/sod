/**
 * ChannelGroupRequestSubsetterXOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class VectorRequestXOR extends VectorRequestLogical implements
        VectorRequest {

    public VectorRequestXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        VectorRequest filterA = (VectorRequest)filterList.get(0);
        VectorRequest filterB = (VectorRequest)filterList.get(1);
        return (filterA.accept(event, channel, request, cookieJar) != filterB.accept(event,
                                                                                     channel,
                                                                                     request,
                                                                                     cookieJar));
    }
}