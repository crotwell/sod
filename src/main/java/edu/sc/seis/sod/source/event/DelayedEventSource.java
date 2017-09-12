package edu.sc.seis.sod.source.event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.util.time.ClockUtil;


public class DelayedEventSource extends AbstractEventSource implements EventSource {

    protected DelayedEventSource(Duration delay, EventSource source) {
        super("delayed "+source.getName(), source.getRetries());
        this.delay = delay;
        this.wrappedSource = source;
    }

    public DelayedEventSource(Element config) throws ConfigurationException {
        super(config, "delayedEventSouce");
        delay = SodUtil.loadTimeInterval(SodUtil.getElement(config, "delay"));
        NodeList children = config.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                Element el = (Element)node;
                if (el.getLocalName().equals("name")) {
                    description = SodUtil.getNestedText(el);
                } else if (el.getLocalName().equals("delay")) {
                    // handled above
                } else {
                    Object o = SodUtil.load(el, "event"); // loads something from source.event package
                    if (o instanceof EventSource) {
                        wrappedSource = (EventSource)o;
                        break;
                    }
                }
            }
        }
    }

    public TimeRange getEventTimeRange() {
        return wrappedSource.getEventTimeRange();
    }

    public Duration getWaitBeforeNext() {
        Duration waitTime = wrappedSource.getWaitBeforeNext();
        for (CacheEvent ce : delayedEvents) {
            Duration deTime = Duration.between(ClockUtil.now(), ce.getOrigin().getOriginTime().plus(delay));
            if (deTime.toNanos() < waitTime.toNanos()) {
                waitTime = deTime;
            }
        }
        return waitTime;
    }

    public boolean hasNext() {
        return delayedEvents.size() != 0 || wrappedSource.hasNext();
    }

    public CacheEvent[] next() {
        logger.debug("next: "+delayedEvents.size()+" delayed.");
        List<CacheEvent> out = new ArrayList<CacheEvent>();
        Iterator<CacheEvent> it = delayedEvents.iterator();
        while(it.hasNext()) {
            CacheEvent cacheEvent  = it.next();
            if (checkEvent(cacheEvent)) {
                out.add(cacheEvent);
                it.remove();
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
        return ClockUtil.now().minus(delay).isAfter(e.getOrigin().getOriginTime());
    }
    
    String description = null;
    
    EventSource wrappedSource;
    
    Duration delay;
    
    LinkedList<CacheEvent> delayedEvents = new LinkedList<CacheEvent>();
    
    private static Logger logger = LoggerFactory.getLogger(DelayedEventSource.class);
}
