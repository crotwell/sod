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
            if (new MicroSecondDate(event.getOrigin().origin_time).difference(new MicroSecondDate(preferred_origin.origin_time)).lessThanEqual(timeVariance)
                    && (((QuantityImpl)event.getOrigin().my_location.depth).subtract((QuantityImpl)preferred_origin.my_location.depth).getValue(depthVariance.get_unit()) <= depthVariance.getValue())) {
                out.add(event);
            }
        }
        return (CacheEvent[])out.toArray(new CacheEvent[0]);
    }
    
    ArrayList eventList;
}
