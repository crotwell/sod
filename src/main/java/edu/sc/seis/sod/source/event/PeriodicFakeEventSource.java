package edu.sc.seis.sod.source.event;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.mock.event.MockEventAttr;
import edu.sc.seis.sod.mock.event.MockOrigin;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.Magnitude;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.sod.util.time.ClockUtil;


public class PeriodicFakeEventSource extends AbstractSource implements EventSource {
    
    protected PeriodicFakeEventSource(MicroSecondDate startTime, TimeInterval interval, int numEvents) {
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

    public MicroSecondTimeRange getEventTimeRange() {
        if (numEvents != -1) {
            return new MicroSecondTimeRange(startTime, (TimeInterval)interval.multiplyBy(numEvents-1));
        }
        return new MicroSecondTimeRange(startTime, ClockUtil.wayFuture());
    }

    public TimeInterval getWaitBeforeNext() {
        if (nextEventTime.before(ClockUtil.now())) {
            return new TimeInterval(0, UnitImpl.SECOND);
        }
        return nextEventTime.subtract(ClockUtil.now());
    }

    public boolean hasNext() {
        return numEvents == -1 || eventCounter < numEvents;
    }

    public CacheEvent[] next() {
        if (nextEventTime.before(ClockUtil.now())) {
            eventCounter++;
            prevEventTime = nextEventTime;
            nextEventTime = startTime.add((TimeInterval)interval.multiplyBy(eventCounter));
            return new CacheEvent[] {
                                     new CacheEvent(MockEventAttr.create(-1),
                                                    MockOrigin.create(prevEventTime, mags))
            };
        }
        return new CacheEvent[0];
    }

    MicroSecondDate startTime;
    
    TimeInterval interval;

    MicroSecondDate nextEventTime;
    
    MicroSecondDate prevEventTime = null;
    
    int numEvents = -1;
    
    int eventCounter = 0;
    
    static Magnitude[] mags = new Magnitude[] {new Magnitude("FAKE", -10, "nobody")};
}
