package edu.sc.seis.sod.util.time;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnsupportedFormat;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.RequestFilterUtil;
import edu.sc.seis.sod.model.station.ChannelIdUtil;

/**
 * @author groves Created on Oct 28, 2004
 */
public class RangeTool {

    public static boolean areContiguous(PlottableChunk one, PlottableChunk two) {
        Duration sampleInterval = Duration.ofNanos(0);
        return areContiguous(one.getTimeRange(),
                             two.getTimeRange(),
                             sampleInterval);
    }

    public static boolean areContiguous(LocalSeismogramImpl one,
                                        LocalSeismogramImpl two) {
        LocalSeismogramImpl first;
        LocalSeismogramImpl second;
        String oneS = "one ";
        String twoS = "two ";
        try {
            oneS += TimeUtils.toISOString(one.begin_time);
            twoS += TimeUtils.toISOString(two.begin_time);
            Instant oneB = one.getBeginTime();
            Instant twoB = two.getBeginTime();
            
        } catch(UnsupportedFormat ee) {
            throw new RuntimeException(oneS+" "+twoS, ee);
        }
        if (one.getBeginTime().isBefore(two.getBeginTime())) {
            first = one;
            second = two;
        } else {
            first = two;
            second = one;
        }
        TimeRange firstRange = new TimeRange(first);
        // make one end time 1/2 sample later, so areContiguous will check that first
        // sample of second is within 1/2 sample period of time of next data point
        return areContiguous(new TimeRange(firstRange.getBeginTime(), 
                                                      firstRange.getEndTime().plus(one.getSampling().getPeriod().dividedBy(2))),
                             new TimeRange(second),
                             first.getSampling().getPeriod());
    }

    public static boolean areContiguous(RequestFilter one, RequestFilter two) {
        return areContiguous(new TimeRange(one),
                             new TimeRange(two));
    }

    public static boolean areContiguous(TimeRange one,
                                        TimeRange two,
                                        Duration interval) {
        if(!RangeTool.areOverlapping(one, two)) {
            Duration littleMoreThanInterval = interval.plus(Duration.ofNanos(1000));
            if(one.getEndTime().isBefore(two.getBeginTime())) {
                return one.getEndTime()
                        .plus(littleMoreThanInterval)
                        .isAfter(two.getBeginTime());
            }
            return two.getEndTime().isBefore(one.getBeginTime()) &&
            two.getEndTime().plus(littleMoreThanInterval).isAfter(one.getBeginTime());
        }
        return false;
    }

    public static boolean areContiguous(TimeRange one,
                                        TimeRange two) {
        return one.getEndTime().equals(two.getBeginTime())
                || one.getBeginTime().equals(two.getEndTime());
    }

    public static boolean areOverlapping(PlottableChunk one, PlottableChunk two) {
        return areOverlapping(one.getTimeRange(), two.getTimeRange());
    }

    public static boolean areOverlapping(TimeRange one,
                                         TimeRange two) {
        if(one.getBeginTime().isBefore(two.getEndTime())
                && one.getEndTime().isAfter(two.getBeginTime())) {
            return true;
        }
        return false;
    }

    public static boolean areOverlapping(LocalSeismogramImpl one,
                                         LocalSeismogramImpl two) {
        TimeRange oneTr = new TimeRange(one.getBeginTime(),
                                                              one.getEndTime());
        TimeRange twoTr = new TimeRange(two.getBeginTime(),
                                                              two.getEndTime());
        return areOverlapping(oneTr, twoTr);
    }
    

    public static boolean seisPartOfRequest(RequestFilter rf, LocalSeismogramImpl seis) {
        if (RequestFilterUtil.containsWildcard(rf)) {
            throw new IllegalArgumentException("RequestFilter must not contain wildcards: "+RequestFilterUtil.toString(rf));
        }
        return ChannelIdUtil.areEqualExceptForBeginTime(rf.getChannelId(), seis.getChannelID()) 
                && RangeTool.areOverlapping(new TimeRange(rf), new TimeRange(seis));
    }

    /**
     * @return A time range encompassing the earliest begin time of the passed
     *          in seismograms to the latest end time
     */
    public static TimeRange getFullTime(LocalSeismogramImpl[] seis) {
        if(seis.length == 0) {
            return ZERO_TIME;
        }
        Instant beginTime = SortTool.byBeginTimeAscending(seis)[0].getBeginTime();
        Instant endTime = TimeUtils.wayPast;
        for(int i = 0; i < seis.length; i++) {
            if(seis[i].getEndTime().isAfter(endTime)) {
                endTime = seis[i].getEndTime();
            }
        }
        return new TimeRange(beginTime, endTime);
    }


    /**
     * @return A time range encompassing the earliest begin time of the passed
     *          in request filter to the latest end time
     */
    public static TimeRange getFullTime(RequestFilter[] seis) {
        if(seis.length == 0) {
            return ZERO_TIME;
        }
        Instant beginTime = SortTool.byBeginTimeAscending(seis)[0].startTime;
        Instant endTime = beginTime;
        for(int i = 0; i < seis.length; i++) {
            if(seis[i].endTime.isAfter(endTime)) {
                endTime = seis[i].endTime;
            }
        }
        return new TimeRange(beginTime, endTime);
    }
    
    
    public static TimeRange getFullTime(List<PlottableChunk> pc) {
        if(pc.size() == 0) {
            return ZERO_TIME;
        }
        Instant beginTime = SortTool.byBeginTimeAscending(pc).get(0).getBeginTime();
        Instant endTime = beginTime;
        for (PlottableChunk plottableChunk : pc) {
            if(plottableChunk.getEndTime().isAfter(endTime)) {
                endTime = plottableChunk.getEndTime();
            }
        }
        return new TimeRange(beginTime, endTime);
    }
    
    public static final TimeRange ZERO_TIME = new TimeRange(Instant.ofEpochSecond(0),
                                                            Instant.ofEpochSecond(0));

    public static final TimeRange ONE_TIME = new TimeRange(Instant.ofEpochMilli(0),
                                                           Instant.ofEpochMilli(1));
}