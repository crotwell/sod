package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class AvailableDataNOT extends AvailableDataLogicalSubsetter
        implements AvailableDataSubsetter {

    public AvailableDataNOT(Element config) throws ConfigurationException {
        super(config);
        filter = (AvailableDataSubsetter)filterList.get(0);
    }

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) throws Exception {
        StringTree result = filter.accept(event,
                                          channel,
                                          original,
                                          available,
                                          cookieJar);
        return new StringTreeBranch(this, !result.isSuccess(), result);
    }

    private AvailableDataSubsetter filter;
}// AvailableDataNOT
