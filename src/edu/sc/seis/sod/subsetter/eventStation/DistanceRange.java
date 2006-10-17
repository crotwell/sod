package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class DistanceRange extends DistanceRangeSubsetter implements
        EventStationSubsetter {

    public DistanceRange(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations eventAccess,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        Origin origin = null;
        origin = eventAccess.get_preferred_origin();
        double actualDistance = SphericalCoords.distance(origin.my_location.latitude,
                                                         origin.my_location.longitude,
                                                         station.my_location.latitude,
                                                         station.my_location.longitude);
        QuantityImpl dist = new QuantityImpl(actualDistance, UnitImpl.DEGREE);
        if(dist.greaterThanEqual(getMin()) && dist.lessThanEqual(getMax())) {
            return new StringTreeLeaf(this, true, "DistanceRange("+getMin()+", "+getMax()+")");
        } else {
            return new StringTreeLeaf(this, false,"DistanceRange("+getMin()+", "+getMax()+")"+ dist.toString());
        }
    }

    private static final Logger logger = Logger.getLogger(DistanceRange.class);
}// EventStationDistance
