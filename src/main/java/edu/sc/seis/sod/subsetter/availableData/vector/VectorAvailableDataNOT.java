/**
 * AvailableDataGroupNOT.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorAvailableDataNOT extends VectorAvailableDataLogicalSubsetter
        implements VectorAvailableDataSubsetter {

    public VectorAvailableDataNOT(Element config) throws ConfigurationException {
        super(config);
        filter = (VectorAvailableDataSubsetter)filterList.get(0);
    }

    public StringTree accept(CacheEvent event,
                             ChannelGroup channel,
                             RequestFilter[][] original,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        StringTree result = filter.accept(event,
                                          channel,
                                          original,
                                          available,
                                          cookieJar);
        return new StringTreeBranch(this, !result.isSuccess(), result);
    }

    private VectorAvailableDataSubsetter filter;
}