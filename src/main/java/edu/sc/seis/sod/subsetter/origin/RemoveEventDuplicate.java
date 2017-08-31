package edu.sc.seis.sod.subsetter.origin;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.ClockUtil;

public class RemoveEventDuplicate implements OriginSubsetter {

    public RemoveEventDuplicate(Element config) throws ConfigurationException {
        Element el = XMLUtil.getElement(config, "timeVariance");
        if (el != null){
            setTimeVariance(ClockUtil.durationFrom(SodUtil.loadQuantity(el)));
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
                          EventAttrImpl eventAttr,
                          OriginImpl preferred_origin)
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
    
    public boolean isDistanceClose(CacheEvent eventA, OriginImpl originB) {
        OriginImpl curOrig = eventA.getOrigin();
        DistAz distAz = new DistAz(curOrig.getLocation(), originB.getLocation());
        if (distanceVariance.getUnit().isConvertableTo(UnitImpl.DEGREE)) {
            return distAz.getDelta() < distanceVariance.convertTo(UnitImpl.DEGREE).getValue();
        } else {
            // use earth radius of 6371 km
            return distAz.getDelta()*6371 < distanceVariance.convertTo(UnitImpl.KILOMETER).getValue();
        }
    }
    
    public List getEventsNearTimeAndDepth(OriginImpl preferred_origin) throws SQLException {
        Instant originTime = preferred_origin.getOriginTime();
        Instant minTime = originTime.minus(timeVariance);
        Instant maxTime = originTime.plus(timeVariance);

        QuantityImpl originDepth = QuantityImpl.createQuantityImpl(preferred_origin.getLocation().depth);
        QuantityImpl depthRangeImpl = QuantityImpl.createQuantityImpl(depthVariance);
        QuantityImpl minDepth = originDepth.subtract(depthRangeImpl);
        QuantityImpl maxDepth = originDepth.add(depthRangeImpl);
        List timeEvents = 
            getEventStatusTable().getEventInTimeRangeRegardlessOfStatus(new TimeRange(minTime,
                                                                maxTime));
        List out = new ArrayList();
        Iterator it = timeEvents.iterator();
        while(it.hasNext()) {
            StatefulEvent e = (StatefulEvent)it.next();
            if (e.getStatus().getStanding().equals(Standing.INIT) ||
                    e.getStatus().getStanding().equals(Standing.IN_PROG) ||
                    e.getStatus().getStanding().equals(Standing.SUCCESS)) {
                QuantityImpl depth = (QuantityImpl)e.getOrigin().getLocation().depth;
                if (depth.greaterThanEqual(minDepth) && depth.lessThanEqual(maxDepth)) {
                    out.add(e);
                    logger.debug("Considering for RemoveEventDup: "+e);
                } else {
                    logger.debug("Not considering (depth="+depth+") for RemoveEventDup: "+e);
                }
            } else {
                logger.debug("Not considering (status="+e.getStatus()+") for RemoveEventDup: "+e);
            }
        }
        return out;
    }
    
    protected void setTimeVariance(QuantityImpl timeVariance) throws ConfigurationException {
        if ( ! ( timeVariance.getUnit().isConvertableTo(UnitImpl.SECOND))) {
            throw new ConfigurationException("Units must be convertible to SECOND: "+timeVariance.getUnit());
        }
        this.timeVariance = ClockUtil.durationFrom(timeVariance);
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

    protected Duration timeVariance = Duration.ofSeconds(10);
    
    protected QuantityImpl distanceVariance = new QuantityImpl(0.5, UnitImpl.DEGREE);
    
    protected QuantityImpl depthVariance = new QuantityImpl(100, UnitImpl.KILOMETER);
    
    private StatefulEventDB eventTable;
    
    private static Logger logger = LoggerFactory.getLogger(RemoveEventDuplicate.class);
}

