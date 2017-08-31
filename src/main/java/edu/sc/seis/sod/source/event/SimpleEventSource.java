package edu.sc.seis.sod.source.event;

import java.time.Duration;
import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.common.TimeRange;
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

    public Duration getWaitBeforeNext() {
        if (hasNextBeenCalled) {
          throw new RuntimeException("SHouldn't happen");
        }
        return Duration.ofSeconds(0);
    }

    public TimeRange getEventTimeRange() {
        CacheEvent[] events = getEvents();
        Instant earliest = extractBeginTime(events[0]);
        Instant latest = earliest;
        for(int i = 0; i < events.length; i++) {
            Instant eventTime = extractBeginTime(events[i]);
            if(eventTime.isBefore(earliest)) {
                earliest = eventTime;
            } else if(eventTime.isAfter(latest)) {
                latest = eventTime;
            }
        }
        return new TimeRange(earliest, latest);
    }

    public Instant extractBeginTime(CacheEvent ev) {
        return ev.getOrigin().getOriginTime();
    }

    public boolean hasNextBeenCalled = false;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleEventSource.class);
}
