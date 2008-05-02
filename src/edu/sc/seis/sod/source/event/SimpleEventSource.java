package edu.sc.seis.sod.source.event;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public abstract class SimpleEventSource implements EventSource {

    public abstract CacheEvent[] getEvents();

    public boolean hasNext() {
        return !hasNextBeenCalled;
    }

    public CacheEvent[] next() {
        hasNextBeenCalled = true;
        CacheEvent[] out = getEvents();
        logger.debug("returning "+out.length+" events");
        return out;
    }

    public TimeInterval getWaitBeforeNext() {
        return new TimeInterval(0, UnitImpl.SECOND);
    }

    public MicroSecondTimeRange getEventTimeRange() {
        CacheEvent[] events = getEvents();
        MicroSecondDate earliest = extractBeginTime(events[0]);
        MicroSecondDate latest = earliest;
        for(int i = 0; i < events.length; i++) {
            MicroSecondDate eventTime = extractBeginTime(events[i]);
            if(eventTime.before(earliest)) {
                earliest = eventTime;
            } else if(eventTime.after(latest)) {
                latest = eventTime;
            }
        }
        return new MicroSecondTimeRange(earliest, latest);
    }

    public MicroSecondDate extractBeginTime(CacheEvent ev) {
        return new MicroSecondDate(ev.getOrigin().origin_time);
    }

    public boolean hasNextBeenCalled = false;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimpleEventSource.class);
}
