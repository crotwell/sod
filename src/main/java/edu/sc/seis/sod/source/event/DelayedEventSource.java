package edu.sc.seis.sod.source.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;


public class DelayedEventSource implements EventSource {

    protected DelayedEventSource(TimeInterval delay, EventSource source) {
        this.delay = delay;
        this.wrappedSource = source;
    }

    public DelayedEventSource(Element config) throws ConfigurationException {
        delay = SodUtil.loadTimeInterval(SodUtil.getElement(config, "delay"));
        Object o = SodUtil.load(config, "event"); // loads something from source.event package
        if (o instanceof EventSource) {
            wrappedSource = (EventSource)o;
        }
    }
    public String getDescription() {
        return "Delayed ("+wrappedSource.getDescription()+") delayed "+delay;
    }

    public MicroSecondTimeRange getEventTimeRange() {
        return wrappedSource.getEventTimeRange();
    }

    public TimeInterval getWaitBeforeNext() {
        return wrappedSource.getWaitBeforeNext();
    }

    public boolean hasNext() {
        return delayedEvents.size() != 0 || wrappedSource.hasNext();
    }

    public CacheEvent[] next() {
        List<CacheEvent> out = new ArrayList<CacheEvent>();
        for (CacheEvent cacheEvent : delayedEvents) {
            if (checkEvent(cacheEvent)) {
                out.add(cacheEvent);
            }
        }
        if (out.size() != 0) {
            return out.toArray(new CacheEvent[0]);
        }
        if (wrappedSource.hasNext()) {
            CacheEvent[] wrapEvents = wrappedSource.next();
            for (int i = 0; i < wrapEvents.length; i++) {
                if (checkEvent(wrapEvents[i])) {
                    out.add(wrapEvents[i]);
                } else {
                    delayedEvents.add(wrapEvents[i]);
                }
            }
        }
        return out.toArray(new CacheEvent[0]);
    }
    
    public boolean checkEvent(CacheEvent e) {
        return ClockUtil.now().subtract(delay).after(e.getOrigin().getTime());
    }
    
    EventSource wrappedSource;
    
    TimeInterval delay;
    
    LinkedList<CacheEvent> delayedEvents = new LinkedList<CacheEvent>();
}
