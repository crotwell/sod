package edu.sc.seis.sod.subsetter.origin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.source.event.EventSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;


public class SimilarEvent extends RemoveEventDuplicate {

    public SimilarEvent(Element config) throws ConfigurationException {
        super(config);
        Element sourceElement = SodUtil.getFirstEmbeddedElement(config);
        EventSource source = (EventSource)SodUtil.load(sourceElement, new String[] { "event" } );
        while(source.hasNext()) {
            CacheEvent[] events = source.next();
            for(int i = 0; i < events.length; i++) {
                eventList.add(events[i]);
            }
        }
    }

    public SimilarEvent(CacheEvent[] events) {
        for(int i = 0; i < events.length; i++) {
            eventList.add(events[i]);
        }
    }
    

    
    public StringTree accept(CacheEvent eventAccess,
                          EventAttrImpl eventAttr,
                          OriginImpl preferred_origin)
        throws Exception {
        // first eliminate based on time and depth as these are easy and the database can do efficiently
        Iterator<CacheEvent> matchingEvents = getEventsNearTimeAndDepth(preferred_origin).iterator();
        while( matchingEvents.hasNext()) {
        	CacheEvent e = (CacheEvent)matchingEvents.next();
            if (e.equals(eventAccess) || isDistanceClose(e, preferred_origin)){
                return new Pass(this);
            }
        }
        return new Fail(this);
    }

    public List<CacheEvent> getEventsNearTimeAndDepth(OriginImpl preferred_origin) {
        ArrayList<CacheEvent> out = new ArrayList<CacheEvent>();
        Iterator<CacheEvent> it = eventList.iterator();
        while(it.hasNext()) {
            CacheEvent event = (CacheEvent)it.next();
            if (isTimeOK(event, preferred_origin)
                    && isDepthOK(event, preferred_origin)) {
                out.add(event);
            }
        }
        return out;
    }
    
    private boolean isTimeOK(CacheEvent event, OriginImpl preferred_origin) {
        Instant eventTime = event.getOrigin().getOriginTime();
        Instant originTime = preferred_origin.getOriginTime();
        return Duration.between(originTime, eventTime).abs().toNanos() < timeVariance.toNanos();
    }
    
    private boolean isDepthOK(CacheEvent event, OriginImpl preferred_origin) {
        QuantityImpl eventDepth = (QuantityImpl)event.getOrigin().getLocation().depth;
        QuantityImpl originDepth = (QuantityImpl)preferred_origin.getLocation().depth;
        double difference = eventDepth.subtract(originDepth).getValue(depthVariance.get_unit());
        return Math.abs(difference) <= depthVariance.getValue();
    }
    
    ArrayList<CacheEvent> eventList = new ArrayList<CacheEvent>();;
}
