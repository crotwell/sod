package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Location;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import edu.sc.seis.sod.subsetter.origin.MagnitudeRange;
import org.w3c.dom.Element;

public class LinearDistanceMagnitudeRange extends DistanceRangeSubsetter
        implements EventStationSubsetter {

    public LinearDistanceMagnitudeRange(Element config)
            throws ConfigurationException {
        super(config);
        Element subElement = SodUtil.getElement(config, "magnitudeRange");
        magnitudeRange = (MagnitudeRange)SodUtil.load(subElement, "origin");
    }

    public StringTree accept(EventAccessOperations eventAccess,
                          Station station,
                          CookieJar cookieJar) {
        Location stationLoc = station.my_location;
        return new StringTreeLeaf(this, accept(eventAccess, stationLoc.latitude, stationLoc.longitude));
    }

    public boolean accept(EventAccessOperations eventAccess,
                          double stationLat,
                          double stationLon) {
        Origin origin = EventUtil.extractOrigin(eventAccess);
        Location originLoc = origin.my_location;
        double actualDistance = SphericalCoords.distance(originLoc.latitude,
                                                         originLoc.longitude,
                                                         stationLat,
                                                         stationLon);
        if(actualDistance >= getMin().value && actualDistance <= getMax().value) {
            double resultantMagnitude = magnitudeRange.getMinValue()
                    + (actualDistance - getMin().value)
                    * (double)(magnitudeRange.getMaxValue() - magnitudeRange.getMinValue())
                    / (getMin().value - getMax().value);
            for(int i = 0; i < origin.magnitudes.length; i++) {
                if(origin.magnitudes[i].value >= resultantMagnitude) {
                    if(magnitudeRange.getSearchTypes().length == 0) {
                        // don't care about search types
                        return true;
                    }
                    for(int j = 0; j < magnitudeRange.getSearchTypes().length; j++) {
                        if(origin.magnitudes[i].type.equals(magnitudeRange.getSearchTypes())) { return true; }
                    }
                }
            }
        }
        return false;
    }

    private MagnitudeRange magnitudeRange;
}// LinearDistanceMagnitudeRange
