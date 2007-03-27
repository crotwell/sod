package edu.sc.seis.sod.subsetter;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;

public abstract class TimeRange implements Subsetter{

    public TimeRange (Element config) throws ConfigurationException{
        timeRange = SodUtil.loadTimeRange(config);
    }

    public Time getStartTime() { return getStartMSD().getFissuresTime(); }

    public Time getEndTime() { return getEndMSD().getFissuresTime(); }

    public edu.iris.Fissures.TimeRange getTimeRange() {
        return new edu.iris.Fissures.TimeRange(getStartTime(), getEndTime());
    }

    public String toString(){ return timeRange.toString(); }

    public MicroSecondTimeRange getMSTR(){ return timeRange; }

    public MicroSecondDate getStartMSD(){ return timeRange.getBeginTime(); }

    public MicroSecondDate getEndMSD(){ return timeRange.getEndTime(); }

    private MicroSecondTimeRange timeRange;

    static Logger logger = Logger.getLogger(TimeRange.class);
}// TimeRange
