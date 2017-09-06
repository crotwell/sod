package edu.sc.seis.sod.subsetter.availableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.RequestFilterUtil;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.CoverageTool;

public class FullCoverage implements AvailableDataSubsetter, SodElement {

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        double coveragePercentage = pc.percentCovered(request, available);
        
        if (available.length == 0) {
            return new StringTreeLeaf(this, false, "No available data");
        }
        RequestFilter[] notCovered = CoverageTool.notCovered(request, available);
        float minSps = ChannelIdUtil.minSPSForBandCode(channel.getChannelCode());
        if (minSps > 0) {
            notCovered = RequestFilterUtil.removeSmallRequests(notCovered, TimeUtils.durationFromSeconds(1/minSps)); // remove time windows smaller than one sample
        }
        if (notCovered.length == 0) {
            String reason = "Data returned completly covers the request";
            return new StringTreeLeaf(this, true, reason);
        }

        String reason = "Data does not cover "+notCovered.length+" sections of the request. ";
        for (int i = 0; i < notCovered.length; i++) {
            reason += notCovered[i].start_time.toString() + " to " + notCovered[i].end_time.toString()+",  ";
        }
        
        return new StringTreeLeaf(this,
                                  coveragePercentage > 99, // use >99 as >=100 often fails with 99.99999 coverage
                                  reason+coveragePercentage
                                          + " percent of data covered");
    }

    private PercentCoverage pc = new PercentCoverage(100);

    private static Logger logger = LoggerFactory.getLogger(FullCoverage.class.getName());
}// FullCoverage
