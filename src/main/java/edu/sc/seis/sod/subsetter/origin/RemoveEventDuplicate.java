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
            setTimeVariance(SodUtil.loadQuantity(el));
        }
        el = XMLUtil.getElement(config, "distanceVariance");
        if (el != null){
            setDistanceVariance(SodUtil.loadQuantity(el));
        }
        el = XMLUtil.getElement(config, "depthVariance");
        if (el != null){
            setDepthVariance(SodUtil.loadQuantity(el));
        }
    }
    
    public RemoveEventDuplicate(QuantityImpl timeVariance, QuantityImpl distanceVariance, QuantityImpl depthVariance)
            throws ConfigurationException {
        setTimeVariance(timeVariance);
        setDistanceVariance(distanceVariance);
        setDepthVariance(depthVariance);
    }
    
    public RemoveEventDuplicate() {
        
    }

    public StatefulEventDB getEventStatusTable() throws SQLException {
        if (eventTable == null) {
            eventTable = StatefulEventDB.getSingleton();
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
        DistAz distAz = new DistAz(curOrig.getLocation(), originB.getLocation());
        if (distanceVariance.getUnit().isConvertableTo(UnitImpl.DEGREE)) {
            return distAz.getDelta() < distanceVariance.convertTo(UnitImpl.DEGREE).value;
        } else {
            // use earth radius of 6371 km
            return distAz.getDelta()*6371 < distanceVariance.convertTo(UnitImpl.KILOMETER).value;
        }
    }
    
    public List getEventsNearTimeAndDepth(Origin preferred_origin) throws SQLException {
        MicroSecondDate originTime = new MicroSecondDate(preferred_origin.getOriginTime());
        MicroSecondDate minTime = originTime.subtract(new TimeInterval(timeVariance));
        MicroSecondDate maxTime = originTime.add(new TimeInterval(timeVariance));

        QuantityImpl originDepth = QuantityImpl.createQuantityImpl(preferred_origin.getLocation().depth);
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
        	QuantityImpl depth = (QuantityImpl)e.getOrigin().getLocation().depth;
        	if (depth.greaterThanEqual(minDepth) && depth.lessThanEqual(maxDepth)) {
        		out.add(e);
        	}
        }
        it = timeEvents.iterator();
        while(it.hasNext()) {
            StatefulEvent e = (StatefulEvent)it.next();
            QuantityImpl depth = (QuantityImpl)e.getOrigin().getLocation().depth;
            if (depth.greaterThanEqual(minDepth) && depth.lessThanEqual(maxDepth)) {
                out.add(e);
            } 
        }
        return out;
    }
    
    protected void setTimeVariance(QuantityImpl timeVariance) throws ConfigurationException {
        if ( ! ( timeVariance.getUnit().isConvertableTo(UnitImpl.SECOND))) {
            throw new ConfigurationException("Units must be convertible to SECOND: "+timeVariance.getUnit());
        }
        this.timeVariance = timeVariance;
    }
    
    protected void setDistanceVariance(QuantityImpl maxDistance) throws ConfigurationException {
        if ( ! ( maxDistance.getUnit().isConvertableTo(UnitImpl.DEGREE) || maxDistance.getUnit().isConvertableTo(UnitImpl.KILOMETER))) {
            throw new ConfigurationException("Units must be convertible to DEGREE or KILOMETER: "+maxDistance.getUnit());
        }
        this.distanceVariance = maxDistance;
    }
    
    protected void setDepthVariance(QuantityImpl depthVariance) throws ConfigurationException {
        if ( ! ( depthVariance.getUnit().isConvertableTo(UnitImpl.KILOMETER))) {
            throw new ConfigurationException("Units must be convertible to KILOMETER: "+depthVariance.getUnit());
        }
        this.depthVariance = depthVariance;
    }

    protected QuantityImpl timeVariance = new QuantityImpl(10, UnitImpl.SECOND);
    
    protected QuantityImpl distanceVariance = new QuantityImpl(0.5, UnitImpl.DEGREE);
    
    protected QuantityImpl depthVariance = new QuantityImpl(100, UnitImpl.KILOMETER);
    
    private StatefulEventDB eventTable;
}

