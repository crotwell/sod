package edu.sc.seis.sod.subsetter.eventStation;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
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

    private static final Logger logger = Logger.getLogger(DistanceRange.class);
}// EventStationDistance
