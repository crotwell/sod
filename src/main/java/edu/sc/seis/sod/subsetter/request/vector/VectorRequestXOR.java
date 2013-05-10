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
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorRequestXOR extends VectorRequestLogical implements
        VectorRequestSubsetter {

    public VectorRequestXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        VectorRequestSubsetter filterA = (VectorRequestSubsetter)filterList.get(0);
        VectorRequestSubsetter filterB = (VectorRequestSubsetter)filterList.get(1);
        StringTree[] result = new StringTree[2];
        result[0] = filterA.accept(event, channel, request, cookieJar);
        result[1] = filterB.accept(event, channel, request, cookieJar);
        
        return new StringTreeBranch(this, result[0].isSuccess() != result[1].isSuccess(), result);
    }
}