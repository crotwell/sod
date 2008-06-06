package edu.sc.seis.sod.subsetter.availableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.time.CoverageTool;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PercentCoverage implements AvailableDataSubsetter {

    private double percentage;

    public PercentCoverage(Element content) {
        DOMHelper.extractDouble(content, ".", 100);
    }

    public PercentCoverage(double percentage) {
        this.percentage = percentage;
    }

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        return new StringTreeLeaf(this, accept(original, available));
    }

    public boolean accept(RequestFilter[] original, RequestFilter[] available) {
        return percentCovered(original, available) >= percentage;
    }

    public double percentCovered(RequestFilter[] original,
                                 RequestFilter[] available) {
        RequestFilter[] uncovered = CoverageTool.notCovered(original, available);
        TimeInterval totalOriginalTime = sum(toMSTR(original));
        TimeInterval totalUncoveredTime = sum(toMSTR(uncovered));
        return (1 - totalUncoveredTime.divideBy(totalOriginalTime).getValue()) * 100;
    }

    private TimeInterval sum(List microSecondTimeRanges) {
        TimeInterval total = new TimeInterval(0, UnitImpl.SECOND);
        for(Iterator iter = microSecondTimeRanges.iterator(); iter.hasNext();) {
            MicroSecondTimeRange time = (MicroSecondTimeRange)iter.next();
            total = total.add(time.getInterval());
        }
        return total;
    }

    private List toMSTR(RequestFilter[] filters) {
        // Ensure that there are no overlaps in the request filters
        filters = ReduceTool.merge(filters);
        List mstrs = new ArrayList(filters.length);
        for(int i = 0; i < filters.length; i++) {
            mstrs.add(new MicroSecondTimeRange(filters[i]));
        }
        return mstrs;
    }
}
