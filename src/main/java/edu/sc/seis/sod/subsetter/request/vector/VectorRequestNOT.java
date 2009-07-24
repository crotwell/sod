/**
 * ChannelGroupRequestSubsetterNOT.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request.vector;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class VectorRequestNOT extends VectorRequestLogical implements
        VectorRequest {

    public VectorRequestNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            VectorRequest filter = (VectorRequest)it.next();
            if(filter.accept(event, channel, request, cookieJar)) { return false; }
        }
        return true;
    }
}