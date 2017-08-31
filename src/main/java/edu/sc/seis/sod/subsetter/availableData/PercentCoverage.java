package edu.sc.seis.sod.subsetter.availableData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.CoverageTool;
import edu.sc.seis.sod.util.time.ReduceTool;

public class PercentCoverage implements AvailableDataSubsetter {

    private double percentage;

    public PercentCoverage(Element content) {
        DOMHelper.extractDouble(content, ".", 100);
    }

    public PercentCoverage(double percentage) {
        this.percentage = percentage;
    }

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        return new StringTreeLeaf(this, accept(request, available));
    }

    public boolean accept(RequestFilter[] original, RequestFilter[] available) {
        return percentCovered(original, available) >= percentage;
    }

    public double percentCovered(RequestFilter[] request,
                                 RequestFilter[] available) {
        RequestFilter[] uncovered = CoverageTool.notCovered(request, available);
        Duration totalOriginalTime = sum(toMSTR(request));
        Duration totalUncoveredTime = sum(toMSTR(uncovered));
        return (1 - totalUncoveredTime.divideBy(totalOriginalTime).getValue()) * 100;
    }

    private Duration sum(List microSecondTimeRanges) {
        Duration total = Duration.ofNanos(0);
        for(Iterator iter = microSecondTimeRanges.iterator(); iter.hasNext();) {
            TimeRange time = (TimeRange)iter.next();
            total = total.plus(time.getInterval());
        }
        return total;
    }

    private List<TimeRange> toMSTR(RequestFilter[] filters) {
        // Ensure that there are no overlaps in the request filters
        filters = ReduceTool.merge(filters);
        List<TimeRange> mstrs = new ArrayList<TimeRange>(filters.length);
        for(int i = 0; i < filters.length; i++) {
            mstrs.add(new TimeRange(filters[i]));
        }
        return mstrs;
    }
}
