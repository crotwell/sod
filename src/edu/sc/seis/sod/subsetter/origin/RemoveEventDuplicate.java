/**
 * EventGrouper.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.origin;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.Quantity;
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
import java.sql.SQLException;
import org.w3c.dom.Element;



public class RemoveEventDuplicate implements OriginSubsetter {

    public RemoveEventDuplicate(Element config) throws ConfigurationException {

        //set defaults. These can be changed in the config file.
        timeVariance = new QuantityImpl(10, UnitImpl.SECOND);
        distanceVariance = new QuantityImpl(0.5, UnitImpl.DEGREE);
        depthVariance = new QuantityImpl(100, UnitImpl.KILOMETER);

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

        try {
            eventTable = new JDBCEventStatus();
        } catch (SQLException e) {
            throw new ConfigurationException("trouble getting event table", e);
        }
    }

    public StringTree accept(EventAccessOperations eventAccess,
                          EventAttr eventAttr,
                          Origin preferred_origin)
        throws Exception {

        MicroSecondDate originTime = new MicroSecondDate(preferred_origin.origin_time);
        MicroSecondDate minTime = originTime.subtract(new TimeInterval(timeVariance));
        MicroSecondDate maxTime = originTime.add(new TimeInterval(timeVariance));

        QuantityImpl originDepth = QuantityImpl.createQuantityImpl(preferred_origin.my_location.depth);
        QuantityImpl depthRangeImpl = QuantityImpl.createQuantityImpl(depthVariance);
        QuantityImpl minDepth = originDepth.subtract(depthRangeImpl);
        QuantityImpl maxDepth = originDepth.add(depthRangeImpl);

        CacheEvent[] matchingEvents = 
            eventTable.getEventsByTimeAndDepthRanges(minTime,
                                                     maxTime,
                                                     minDepth.getValue(UnitImpl.KILOMETER),
                                                     maxDepth.getValue(UnitImpl.KILOMETER));

        for (int i = 0; i < matchingEvents.length; i++) {
            if (!matchingEvents[i].equals(eventAccess)){
                Origin curOrig = matchingEvents[i].getOrigin();
                DistAz distAz = new DistAz(curOrig.my_location, preferred_origin.my_location);
                if (distAz.getDelta() < distanceVariance.value){
                    return new StringTreeLeaf(this, false);
                }
            }
        }
        return new StringTreeLeaf(this, true);
    }
    

    private Quantity timeVariance, distanceVariance, depthVariance;
    private JDBCEventStatus eventTable;
}

