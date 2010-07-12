package edu.sc.seis.sod.subsetter.availableData;

import org.apache.log4j.Category;

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
                                  coveragePercentage >= 100,
                                  coveragePercentage
                                          + " percent of data covered");
    }

    private PercentCoverage pc = new PercentCoverage(100);

    static Category logger = Category.getInstance(FullCoverage.class.getName());
}// FullCoverage
