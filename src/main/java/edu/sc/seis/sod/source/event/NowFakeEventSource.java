package edu.sc.seis.sod.source.event;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;


public class NowFakeEventSource extends PeriodicFakeEventSource {

    public NowFakeEventSource(Element config) throws ConfigurationException {
        super(ClockUtil.now(), SodUtil.loadTimeInterval(SodUtil.getElement(config, "interval")), SodUtil.loadInt(config, "numEvents", -1));
    }

    public String getDescription() {
        return "Events starting now, with "+numEvents+" new events at the current request time but no more frequent than "+interval;
    }

    public MicroSecondTimeRange getEventTimeRange() {
        return new MicroSecondTimeRange(startTime, ClockUtil.wayFuture());
    }

    public CacheEvent[] next() {
        if (nextEventTime.before(ClockUtil.now())) {
            eventCounter++;
            prevEventTime = ClockUtil.now();
            nextEventTime = prevEventTime.add(interval);
            return new CacheEvent[] {
                                     new CacheEvent(MockEventAttr.create(-1),
                                                    MockOrigin.create(prevEventTime, mags))
            };
        }
        return new CacheEvent[0];
    }
}
