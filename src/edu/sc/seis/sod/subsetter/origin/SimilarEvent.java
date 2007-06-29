package edu.sc.seis.sod.subsetter.origin;

import java.util.ArrayList;
import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.event.EventSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;


public class SimilarEvent extends RemoveEventDuplicate {

    public SimilarEvent(Element config) throws ConfigurationException {
        super(config);
        Element sourceElement = SodUtil.getFirstEmbeddedElement(config);
        EventSource source = (EventSource)SodUtil.load(sourceElement, new String[] { "event" } );
        eventList = new ArrayList();
        while(source.hasNext()) {
            CacheEvent[] events = source.next();
            for(int i = 0; i < events.length; i++) {
                eventList.add(events[i]);
            }
        }
    }

    public StringTree accept(EventAccessOperations eventAccess, EventAttr eventAttr, Origin preferred_origin) throws Exception {
        return new StringTreeLeaf(this, ! super.accept(eventAccess, eventAttr, preferred_origin).isSuccess());
    }

    public CacheEvent[] getEventsNearTimeAndDepth(Origin preferred_origin) {
        ArrayList out = new ArrayList();
        Iterator it = eventList.iterator();
        while(it.hasNext()) {
            CacheEvent event = (CacheEvent)it.next();
            if (isTimeOK(event, preferred_origin)
                    && isDepthOK(event, preferred_origin)) {
                out.add(event);
            }
        }
        return (CacheEvent[])out.toArray(new CacheEvent[0]);
    }
    
    private boolean isTimeOK(CacheEvent event, Origin preferred_origin) {
        MicroSecondDate eventTime = new MicroSecondDate(event.getOrigin().origin_time);
        MicroSecondDate originTime = new MicroSecondDate(preferred_origin.origin_time);
        return eventTime.difference(originTime).lessThanEqual(timeVariance);
    }
    
    private boolean isDepthOK(CacheEvent event, Origin preferred_origin) {
        QuantityImpl eventDepth = (QuantityImpl)event.getOrigin().my_location.depth;
        QuantityImpl originDepth = (QuantityImpl)preferred_origin.my_location.depth;
        double difference = eventDepth.subtract(originDepth).getValue(depthVariance.get_unit());
        return Math.abs(difference) <= depthVariance.getValue();
    }
    
    ArrayList eventList;
}
