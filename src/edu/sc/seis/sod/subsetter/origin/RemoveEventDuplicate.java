package edu.sc.seis.sod.subsetter.origin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RemoveEventDuplicate implements OriginSubsetter {

    public RemoveEventDuplicate(Element config) throws ConfigurationException {
        Element el = XMLUtil.getElement(config, "timeVariance");
        if (el != null){
            timeVariance = SodUtil.loadQuantity(el);
        }
        el = XMLUtil.getElement(config, "distanceVariance");
        if (el != null){
            distanceVariance = SodUtil.loadQuantity(el);
        }
        el = XMLUtil.getElement(config, "depthVariance");
        if (el != null){
            depthVariance = SodUtil.loadQuantity(el);
        }
    }
    
    public RemoveEventDuplicate(QuantityImpl timeVariance, QuantityImpl distanceVariance, QuantityImpl depthVariance) {
        this.timeVariance = timeVariance;
        this.distanceVariance = distanceVariance;
        this.depthVariance = depthVariance;
    }
    
    public RemoveEventDuplicate() {
        
    }

    public StatefulEventDB getEventStatusTable() throws SQLException {
        if (eventTable == null) {
            eventTable = new StatefulEventDB();
        }
        return eventTable;
    }
    
    public StringTree accept(CacheEvent eventAccess,
                          EventAttr eventAttr,
                          Origin preferred_origin)
        throws Exception {
        // first eliminate based on time and depth as these are easy and the database can do efficiently
        Iterator matchingEvents = getEventsNearTimeAndDepth(preferred_origin).iterator();
        while( matchingEvents.hasNext()) {
        	StatefulEvent e = (StatefulEvent)matchingEvents.next();
            if (!e.equals(eventAccess)){
                if (isDistanceClose(e, preferred_origin)){
                    return new StringTreeLeaf(this, false);
                }
            }
        }
        return new StringTreeLeaf(this, true);
    }
    
    public boolean isDistanceClose(CacheEvent eventA, Origin originB) {
        Origin curOrig = eventA.getOrigin();
        DistAz distAz = new DistAz(curOrig.my_location, originB.my_location);
        return distAz.getDelta() < distanceVariance.value;
    }
    
    public List getEventsNearTimeAndDepth(Origin preferred_origin) throws SQLException {
        MicroSecondDate originTime = new MicroSecondDate(preferred_origin.origin_time);
        MicroSecondDate minTime = originTime.subtract(new TimeInterval(timeVariance));
        MicroSecondDate maxTime = originTime.add(new TimeInterval(timeVariance));

        QuantityImpl originDepth = QuantityImpl.createQuantityImpl(preferred_origin.my_location.depth);
        QuantityImpl depthRangeImpl = QuantityImpl.createQuantityImpl(depthVariance);
        QuantityImpl minDepth = originDepth.subtract(depthRangeImpl);
        QuantityImpl maxDepth = originDepth.add(depthRangeImpl);
        List inProgEvents = getEventStatusTable().getEventInTimeRange(new MicroSecondTimeRange(minTime,
                                                                                               maxTime),
                                                                                               Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                                                          Standing.INIT));
        List timeEvents = 
            getEventStatusTable().getEventInTimeRange(new MicroSecondTimeRange(minTime,
                                                                maxTime),
                                                                Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                           Standing.SUCCESS));
        List out = new ArrayList();
        Iterator it = inProgEvents.iterator();
        while(it.hasNext()) {
        	StatefulEvent e = (StatefulEvent)it.next();
        	QuantityImpl depth = (QuantityImpl)e.getOrigin().my_location.depth;
        	if (depth.greaterThanEqual(minDepth) && depth.lessThanEqual(maxDepth)) {
        		out.add(e);
        	}
        }
        it = timeEvents.iterator();
        while(it.hasNext()) {
            StatefulEvent e = (StatefulEvent)it.next();
            QuantityImpl depth = (QuantityImpl)e.getOrigin().my_location.depth;
            if (depth.greaterThanEqual(minDepth) && depth.lessThanEqual(maxDepth)) {
                out.add(e);
            } 
        }
        return out;
    }

    protected QuantityImpl timeVariance = new QuantityImpl(10, UnitImpl.SECOND);
    
    protected QuantityImpl distanceVariance = new QuantityImpl(0.5, UnitImpl.DEGREE);
    
    protected QuantityImpl depthVariance = new QuantityImpl(100, UnitImpl.KILOMETER);
    
    private StatefulEventDB eventTable;
}

