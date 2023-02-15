package edu.sc.seis.sod.util.time;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

/**
 * @author groves Created on Oct 28, 2004
 */
public class CoverageTool {

    /**
     * @return an array containing the request filters taken from the
     *          <code>filters</code> array that are not completely covered by
     *          the given seismograms begin and end.
     */
    public static RequestFilter[] notCovered(RequestFilter[] neededFilters,
                                             LocalSeismogramImpl[] existingFilters) {
        if(existingFilters.length == 0) {
            return neededFilters;
        }
        LocalSeismogramImpl[] sorted = SortTool.byBeginTimeAscending(existingFilters);
        TimeRange[] ranges = new TimeRange[sorted.length];
        for(int i = 0; i < sorted.length; i++) {
            ranges[i] = new TimeRange(sorted[i]);
        }
        return CoverageTool.notCovered(neededFilters, ranges);
    }

    /**
     * @return an array containing the request filters taken from the
     *          <code>filters</code> array that are not completely covered by
     *          the existing filters begin and end.
     */
    public static RequestFilter[] notCovered(RequestFilter[] neededFilters,
                                             RequestFilter[] existingFilters) {
        if(existingFilters.length == 0) {
            return neededFilters;
        }
        RequestFilter[] sorted = SortTool.byBeginTimeAscending(existingFilters);
        TimeRange[] ranges = new TimeRange[sorted.length];
        for(int i = 0; i < sorted.length; i++) {
            ranges[i] = new TimeRange(sorted[i]);
        }
        return CoverageTool.notCovered(neededFilters, ranges);
    }

    public static RequestFilter[] notCovered(RequestFilter[] filters,
                                             TimeRange[] timeRanges) {
        List unsatisfied = new ArrayList();
        timeRanges = ReduceTool.merge(timeRanges);
        timeRanges = SortTool.byBeginTimeAscending(timeRanges);
        for(int i = 0; i < filters.length; i++) {
            Instant rfStart = filters[i].startTime;
            Instant rfEnd = filters[i].endTime;
            for(int j = 0; j < timeRanges.length; j++) {
                Instant trStart = timeRanges[j].getBeginTime();
                Instant trEnd = timeRanges[j].getEndTime();
                if(trStart.isBefore(rfEnd)) {
                    if(trEnd.isAfter(rfStart)) {
                        if(ReduceTool.equalsOrBefore(trStart, rfStart)) {
                            rfStart = trEnd;
                        } else {
                            unsatisfied.add(new RequestFilter(filters[i].channelId,
                                                              rfStart,
                                                              trStart));
                            rfStart = trEnd;
                        }
                        if(ReduceTool.equalsOrAfter(trEnd, rfEnd)) {
                            break;
                        }
                    }
                }
            }
            if(rfEnd.isAfter(rfStart)) {
                unsatisfied.add(new RequestFilter(filters[i].channelId,
                                                  rfStart,
                                                  rfEnd));
            }
        }
        return (RequestFilter[])unsatisfied.toArray(new RequestFilter[unsatisfied.size()]);
    }

    public static RequestFilter[] notCoveredIgnoreGaps(RequestFilter[] filters,
                                                       TimeRange[] timeRanges) {
        if(timeRanges.length != 0) {
            timeRanges = SortTool.byBeginTimeAscending(timeRanges);
            timeRanges = new TimeRange[] {new TimeRange(timeRanges[0],
                                                                              timeRanges[timeRanges.length - 1])};
        }
        return notCovered(filters, timeRanges);
    }
}