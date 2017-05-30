package edu.sc.seis.sod.subsetter.eventStation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;

public class DistanceRange extends DistanceRangeSubsetter implements
        EventStationSubsetter {

    public DistanceRange(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent eventAccess,
                          StationImpl station,
                          CookieJar cookieJar) throws Exception {
        OriginImpl origin =  eventAccess.getOrigin();
        double actualDistance = SphericalCoords.distance(origin.getLocation().latitude,
                                                         origin.getLocation().longitude,
                                                         station.getLocation().latitude,
                                                         station.getLocation().longitude);
        QuantityImpl dist = new QuantityImpl(actualDistance, UnitImpl.DEGREE);
        if(dist.greaterThanEqual(getMin()) && dist.lessThanEqual(getMax())) {
            return new StringTreeLeaf(this, true, "DistanceRange("+getMin()+", "+getMax()+")");
        } else {
            return new StringTreeLeaf(this, false,"DistanceRange("+getMin()+", "+getMax()+")"+ dist.toString());
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DistanceRange.class);
}// EventStationDistance
