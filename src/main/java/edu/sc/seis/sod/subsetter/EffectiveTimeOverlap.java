package edu.sc.seis.sod.subsetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.ISOTime;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;

public abstract class EffectiveTimeOverlap implements Subsetter{
    
    public EffectiveTimeOverlap(MicroSecondTimeRangeSupplier timeRange) {
        this.timeRange = timeRange;
    }
    
    public EffectiveTimeOverlap(MicroSecondTimeRange range) {
        this(range.getBeginTime(), range.getEndTime());
    }

    public EffectiveTimeOverlap(final MicroSecondDate start, final MicroSecondDate end) {
        timeRange = new MicroSecondTimeRangeSupplier() {
            MicroSecondTimeRange range = new MicroSecondTimeRange(start, end);
            public MicroSecondTimeRange getMSTR() { return range; }
            };
    }

    public EffectiveTimeOverlap(Element config) throws ConfigurationException{
        timeRange = SodUtil.loadTimeRange(config);
    }

    public boolean overlaps(MicroSecondTimeRange otherRange) {
        MicroSecondDate otherStart = otherRange.getBeginTime();
        MicroSecondDate otherEnd;
        if (otherRange.getEndTime() == null) {
            otherEnd = new MicroSecondDate(ISOTime.future);
        } else {
            otherEnd = otherRange.getEndTime();
        } // end of else
        return overlaps(otherStart, otherEnd);
    }
    
    public boolean overlaps(MicroSecondDate otherStart, MicroSecondDate otherEnd) {
        MicroSecondTimeRange mstr = timeRange.getMSTR();
        MicroSecondDate start = mstr.getBeginTime();
        MicroSecondDate end = mstr.getEndTime();
        if (end == null && start == null) {
            return true;
        } else if (end == null && start.before(otherEnd)) {
            return true;
        } else if (start == null && end.after(otherStart)) {
            return true;
        } else if(otherStart.after(end) || otherEnd.before(start) ) {
            return false;
        } else {
            return true;
        }
    }

    private MicroSecondTimeRangeSupplier timeRange;

    private static Logger logger = LoggerFactory.getLogger(EffectiveTimeOverlap.class);
}// EffectiveTimeOverlap
