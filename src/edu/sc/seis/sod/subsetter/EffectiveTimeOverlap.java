package edu.sc.seis.sod.subsetter;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;

public abstract class EffectiveTimeOverlap implements Subsetter{
    public EffectiveTimeOverlap(edu.iris.Fissures.TimeRange range) {
        start = new MicroSecondDate(range.start_time);
        end = new MicroSecondDate(range.end_time);
    }

    public EffectiveTimeOverlap(Element config) throws ConfigurationException{
        MicroSecondTimeRange timeRange = SodUtil.loadTimeRange(config);
        start = timeRange.getBeginTime();
        end = timeRange.getEndTime();
    }

    public boolean overlaps(edu.iris.Fissures.TimeRange otherRange) {
        MicroSecondDate otherStart = new MicroSecondDate(otherRange.start_time);
        MicroSecondDate otherEnd;
        if (otherRange.end_time.date_time.equals(edu.iris.Fissures.TIME_UNKNOWN.value)) {
            otherEnd = new MicroSecondDate(TimeUtils.future);
        } else {
            otherEnd = new MicroSecondDate(otherRange.end_time);
        } // end of else
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

    private MicroSecondDate start, end;

    private static Logger logger = Logger.getLogger(EffectiveTimeOverlap.class);
}// EffectiveTimeOverlap
