/**
 * ChannelGroupRequestSubsetterOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request.vector;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class VectorRequestOR extends VectorRequestLogical implements
        VectorRequest {

    public VectorRequestOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            VectorRequest filter = (VectorRequest)it.next();
            if(filter.accept(event, channel, request, cookieJar)) { return true; }
        }
        return false;
    }
}