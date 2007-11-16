/**
 * ChannelGroupRequestSubsetter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request.vector;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface VectorRequest extends Subsetter {

    public boolean accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception;
}