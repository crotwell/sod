package edu.sc.seis.sod.source.event;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;


public class PeriodicFakeEventSource implements EventSource {

    public PeriodicFakeEventSource(Element config) throws ConfigurationException {
        startTime = new MicroSecondDate(SodUtil.loadTime(SodUtil.getElement(config, "startTime")));
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
        return new MicroSecondTimeRange(startTime, ClockUtil.future());
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
        nextEventTime = startTime.add((TimeInterval)interval.multiplyBy(eventCounter));
        if (nextEventTime.before(ClockUtil.now())) {
            eventCounter++;
            return new CacheEvent[] {
                                     new CacheEvent(MockEventAttr.create(-1),
                                                    MockOrigin.create(nextEventTime, mags))
            };
        }
        return new CacheEvent[0];
    }

    MicroSecondDate startTime;
    
    TimeInterval interval;

    MicroSecondDate nextEventTime;
    
    int numEvents = -1;
    
    int eventCounter = 0;
    
    static Magnitude[] mags = new Magnitude[] {new Magnitude("FAKE", -10, "nobody")};
}
