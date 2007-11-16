/**
 * PassChannelGroupAvailableData.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PassVectorAvailableData implements VectorAvailableDataSubsetter {

    public StringTree accept(CacheEvent event,
                             ChannelGroup channelGroup,
                             RequestFilter[][] original,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}