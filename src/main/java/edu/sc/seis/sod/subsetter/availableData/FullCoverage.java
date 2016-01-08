package edu.sc.seis.sod.subsetter.availableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class FullCoverage implements AvailableDataSubsetter, SodElement {

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        double coveragePercentage = pc.percentCovered(request, available);
        return new StringTreeLeaf(this,
                                  coveragePercentage > 99, // use >99 as >=100 often fails with 99.99999 coverage
                                  coveragePercentage
                                          + " percent of data covered");
    }

    private PercentCoverage pc = new PercentCoverage(100);

    private static Logger logger = LoggerFactory.getLogger(FullCoverage.class.getName());
}// FullCoverage
