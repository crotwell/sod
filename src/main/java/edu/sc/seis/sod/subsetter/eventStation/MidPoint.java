/**
 * MidPoint.java
 *
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.Area;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

public class MidPoint extends AreaSubsetter  implements EventStationSubsetter {

    public MidPoint(Element config) throws ConfigurationException {
    	super(config);
    }

    public StringTree accept(CacheEvent eventAccess,
                             Station station,
                          CookieJar cookieJar) throws Exception {
        OriginImpl origin = eventAccess.get_preferred_origin();
        Location originLoc = origin.getLocation();
        Location loc = Location.of(station);
        double azimuth = SphericalCoords.azimuth(originLoc.latitude,
                                                 originLoc.longitude,
                                                 loc.latitude,
                                                 loc.longitude);
        double dist = SphericalCoords.distance(originLoc.latitude,
                                               originLoc.longitude,
                                               loc.latitude,
                                               loc.longitude);
        dist /= 2;
        double latitude = SphericalCoords.latFor(originLoc.latitude,
                                                 originLoc.longitude,
                                                 dist,
                                                 azimuth);
        double longitude = SphericalCoords.lonFor(originLoc.latitude,
                                                  originLoc.longitude,
                                                  dist,
                                                  azimuth);
        return new StringTreeLeaf(this,
                                  accept(new Location((float)latitude, (float)longitude, ZERO, ZERO, originLoc.type)),
                                  "mid=("+latitude+", "+longitude+")");
    }

    Area area;
    
    static final QuantityImpl ZERO = new QuantityImpl(0, UnitImpl.KILOMETER);
    
}
