package edu.sc.seis.sod.source.event;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.source.AbstractSource;

public abstract class SimpleEventSource extends AbstractSource implements EventSource {

    public SimpleEventSource(Element config, String defaultName, int defaultRetries) {
        super(config, defaultName, defaultRetries);
    }

    public SimpleEventSource(Element config, String defaultName) {
        super(config, defaultName);
    }

    public SimpleEventSource(String name, int retries) {
        super(name, retries);
    }

    public SimpleEventSource(String name) {
        super(name);
    }

    public abstract CacheEvent[] getEvents();

    public boolean hasNext() {
        return ! hasNextBeenCalled;
    }

    public CacheEvent[] next() {
        hasNextBeenCalled = true;
        CacheEvent[] out = getEvents();
        logger.debug("returning "+out.length+" events");
        return out;
    }

    public TimeInterval getWaitBeforeNext() {
        if (hasNextBeenCalled) {
          throw new RuntimeException("SHouldn't happen");
        }
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
        return new MicroSecondDate(ev.getOrigin().getOriginTime());
    }

    public boolean hasNextBeenCalled = false;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleEventSource.class);
}
