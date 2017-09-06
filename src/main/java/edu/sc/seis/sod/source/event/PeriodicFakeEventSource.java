package edu.sc.seis.sod.source.event;

import java.time.Duration;
import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.mock.event.MockEventAttr;
import edu.sc.seis.sod.mock.event.MockOrigin;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.Magnitude;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.sod.util.time.ClockUtil;


public class PeriodicFakeEventSource extends AbstractSource implements EventSource {
    
    protected PeriodicFakeEventSource(Instant startTime, Duration interval, int numEvents) {
        super("PeriodicFakeEventSource");
        this.startTime = startTime;
        this.interval = interval;
        this.numEvents = numEvents;
        nextEventTime = startTime;
    }

    public PeriodicFakeEventSource(Element config) throws ConfigurationException {
        super(config, "PeriodicFakeEventSource ");
        startTime = SodUtil.loadTime(SodUtil.getElement(config, "startTime")).load();
        interval = SodUtil.loadTimeInterval(SodUtil.getElement(config, "interval"));
        numEvents = SodUtil.loadInt(config, "numEvents", -1);
        nextEventTime = startTime;
    }
    
    public String getDescription() {
        return "Periodic Fake Events "+numEvents+" events from "+startTime+" in steps of "+interval;
    }

    public TimeRange getEventTimeRange() {
        if (numEvents != -1) {
            return new TimeRange(startTime, interval.multipliedBy(numEvents-1));
        }
        return new TimeRange(startTime, ClockUtil.wayFuture());
    }

    public Duration getWaitBeforeNext() {
        if (nextEventTime.isBefore(ClockUtil.now())) {
            return Duration.ofSeconds(0);
        }
        return Duration.between(ClockUtil.now(), nextEventTime);
    }

    public boolean hasNext() {
        return numEvents == -1 || eventCounter < numEvents;
    }

    public CacheEvent[] next() {
        if (nextEventTime.isBefore(ClockUtil.now())) {
            eventCounter++;
            prevEventTime = nextEventTime;
            nextEventTime = startTime.plus(interval.multipliedBy(eventCounter));
            return new CacheEvent[] {
                                     new CacheEvent(MockEventAttr.create(-1),
                                                    MockOrigin.create(prevEventTime, mags))
            };
        }
        return new CacheEvent[0];
    }

    Instant startTime;
    
    Duration interval;

    Instant nextEventTime;
    
    Instant prevEventTime = null;
    
    int numEvents = -1;
    
    int eventCounter = 0;
    
    static Magnitude[] mags = new Magnitude[] {new Magnitude("FAKE", -10, "nobody")};
}
