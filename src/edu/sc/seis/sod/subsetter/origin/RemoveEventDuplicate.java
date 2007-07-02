package edu.sc.seis.sod.subsetter.origin;

import java.sql.SQLException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
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

    public JDBCEventStatus getEventStatusTable() throws SQLException {
        if (eventTable == null) {
            eventTable = new JDBCEventStatus();
        }
        return eventTable;
    }
    
    public StringTree accept(EventAccessOperations eventAccess,
                          EventAttr eventAttr,
                          Origin preferred_origin)
        throws Exception {
        // first eliminate based on time and depth as these are easy and the database can do efficiently
        CacheEvent[] matchingEvents = getEventsNearTimeAndDepth(preferred_origin);
        for (int i = 0; i < matchingEvents.length; i++) {
            if (!matchingEvents[i].equals(eventAccess)){
                if (isDistanceClose(matchingEvents[i], preferred_origin)){
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
    
    public CacheEvent[] getEventsNearTimeAndDepth(Origin preferred_origin) throws SQLException {
        MicroSecondDate originTime = new MicroSecondDate(preferred_origin.origin_time);
        MicroSecondDate minTime = originTime.subtract(new TimeInterval(timeVariance));
        MicroSecondDate maxTime = originTime.add(new TimeInterval(timeVariance));

        QuantityImpl originDepth = QuantityImpl.createQuantityImpl(preferred_origin.my_location.depth);
        QuantityImpl depthRangeImpl = QuantityImpl.createQuantityImpl(depthVariance);
        QuantityImpl minDepth = originDepth.subtract(depthRangeImpl);
        QuantityImpl maxDepth = originDepth.add(depthRangeImpl);

        return
            getEventStatusTable().getEventsByTimeAndDepthRanges(minTime,
                                                                maxTime,
                                                                minDepth.getValue(UnitImpl.KILOMETER),
                                                                maxDepth.getValue(UnitImpl.KILOMETER));
    }

    protected QuantityImpl timeVariance = new QuantityImpl(10, UnitImpl.SECOND);
    
    protected QuantityImpl distanceVariance = new QuantityImpl(0.5, UnitImpl.DEGREE);
    
    protected QuantityImpl depthVariance = new QuantityImpl(100, UnitImpl.KILOMETER);
    
    private JDBCEventStatus eventTable;
}

