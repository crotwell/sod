/**
 * PassChannelGroupRequest.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request.vector;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;

public class PassVectorRequest implements VectorRequest {

    public PassVectorRequest() {}

    public PassVectorRequest(Element config) {}

    public boolean accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        return true;
    }
}