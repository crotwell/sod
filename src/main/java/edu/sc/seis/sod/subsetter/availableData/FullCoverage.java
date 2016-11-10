package edu.sc.seis.sod.subsetter.availableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.time.CoverageTool;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.process.waveform.WaveformResult;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class FullCoverage implements AvailableDataSubsetter, SodElement {

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        double coveragePercentage = pc.percentCovered(request, available);
        
        if (available.length == 0) {
            return new StringTreeLeaf(this, false, "No available data");
        }
        RequestFilter[] notCovered = CoverageTool.notCovered(request, available);
        float minSps = ChannelIdUtil.minSPSForBandCode(channel.get_code());
        if (minSps > 0) {
            notCovered = RequestFilterUtil.removeSmallRequests(notCovered, new TimeInterval(1/minSps, UnitImpl.SECOND)); // remove time windows smaller than one sample
        }
        if (notCovered.length == 0) {
            String reason = "Data returned completly covers the request";
            return new StringTreeLeaf(this, true, reason);
        }

        String reason = "Data does not cover "+notCovered.length+" sections of the request. ";
        for (int i = 0; i < notCovered.length; i++) {
            reason += notCovered[i].start_time.date_time + " to " + notCovered[i].end_time.date_time+",  ";
        }
        
        return new StringTreeLeaf(this,
                                  coveragePercentage > 99, // use >99 as >=100 often fails with 99.99999 coverage
                                  reason+coveragePercentage
                                          + " percent of data covered");
    }

    private PercentCoverage pc = new PercentCoverage(100);

    private static Logger logger = LoggerFactory.getLogger(FullCoverage.class.getName());
}// FullCoverage
