package edu.sc.seis.sod.subsetter.availableData;

import org.apache.log4j.Category;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SomeCoverage implements AvailableDataSubsetter {

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        // simple impl, probably need more robust
        return new StringTreeLeaf(this, available.length != 0, available.length
                + " filters returned by available data");
    }

    static Category logger = Category.getInstance(SomeCoverage.class.getName());
}// SomeCoverage
