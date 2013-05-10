package edu.sc.seis.sod.subsetter.availableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SomeCoverage implements AvailableDataSubsetter {

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        // simple impl, probably need more robust
        return new StringTreeLeaf(this, available.length != 0, available.length
                + " filters returned by available data");
    }

    private static Logger logger = LoggerFactory.getLogger(SomeCoverage.class.getName());
}// SomeCoverage
