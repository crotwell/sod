package edu.sc.seis.sod.subsetter;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;

public abstract class EffectiveTimeOverlap implements Subsetter{
    
    public EffectiveTimeOverlap(MicroSecondTimeRangeSupplier timeRange) {
        this.timeRange = timeRange;
    }
    
    public EffectiveTimeOverlap(TimeRange range) {
        this(range.getBeginTime(), range.getEndTime());
    }

    public EffectiveTimeOverlap(final Instant start, final Instant end) {
        timeRange = new MicroSecondTimeRangeSupplier() {
            TimeRange range = new TimeRange(start, end);
            public TimeRange getMSTR() { return range; }
            };
    }

    public EffectiveTimeOverlap(Element config) throws ConfigurationException{
        timeRange = SodUtil.loadTimeRange(config);
    }

    public boolean overlaps(TimeRange otherRange) {
        Instant otherStart = otherRange.getBeginTime();
        Instant otherEnd;
        if (otherRange.getEndTime() == null) {
            otherEnd = TimeUtils.future;
        } else {
            otherEnd = otherRange.getEndTime();
        } // end of else
        return overlaps(otherStart, otherEnd);
    }
    
    public boolean overlaps(Instant otherStart, Instant otherEnd) {
        TimeRange mstr = timeRange.getMSTR();
        Instant start = mstr.getBeginTime();
        Instant end = mstr.getEndTime();
        if (end == null && start == null) {
            return true;
        } else if (end == null && start.isBefore(otherEnd)) {
            return true;
        } else if (start == null && end.isAfter(otherStart)) {
            return true;
        } else if(otherStart.isAfter(end) || otherEnd.isBefore(start) ) {
            return false;
        } else {
            return true;
        }
    }

    private MicroSecondTimeRangeSupplier timeRange;

    private static Logger logger = LoggerFactory.getLogger(EffectiveTimeOverlap.class);
}// EffectiveTimeOverlap
