package edu.sc.seis.sod.subsetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;

public abstract class EffectiveTimeOverlap implements Subsetter{
    
    public EffectiveTimeOverlap(MicroSecondTimeRangeSupplier timeRange) {
        this.timeRange = timeRange;
    }
    
    public EffectiveTimeOverlap(edu.iris.Fissures.TimeRange range) {
        this(new MicroSecondDate(range.start_time), new MicroSecondDate(range.end_time));
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

    public boolean overlaps(edu.iris.Fissures.TimeRange otherRange) {
        MicroSecondDate otherStart = new MicroSecondDate(otherRange.start_time);
        MicroSecondDate otherEnd;
        if (otherRange.end_time.date_time.equals(edu.iris.Fissures.TIME_UNKNOWN.value)) {
            otherEnd = new MicroSecondDate(TimeUtils.future);
        } else {
            otherEnd = new MicroSecondDate(otherRange.end_time);
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
