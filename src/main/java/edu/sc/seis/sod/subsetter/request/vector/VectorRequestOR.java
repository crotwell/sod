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
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorRequestOR extends VectorRequestLogical implements
        VectorRequest {

    public VectorRequestOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree[] result = new  StringTree[filterList.size()];
        int i=0;
        while(it.hasNext()) {
            VectorRequest filter = (VectorRequest)it.next();
            result[i] = filter.accept(event, channel, request, cookieJar);
            if(result[i].isSuccess()) { return new StringTreeBranch(this, true, result); }
            i++;
        }
        return new StringTreeBranch(this, false, result);
    }
}