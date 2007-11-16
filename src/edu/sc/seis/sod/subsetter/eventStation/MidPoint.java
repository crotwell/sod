/**
 * MidPoint.java
 *
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.PointDistanceArea;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MidPoint extends AreaSubsetter  implements EventStationSubsetter {

    public MidPoint(Element config) throws ConfigurationException {
    	super(config);
    }

    public StringTree accept(CacheEvent eventAccess,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        Origin origin = eventAccess.get_preferred_origin();
        Location originLoc = origin.my_location;
        Location loc = station.my_location;
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
        return new StringTreeLeaf(this, accept(new Location((float)latitude, (float)longitude, ZERO, ZERO, originLoc.type)));
    }

    Area area;
    
    static final QuantityImpl ZERO = new QuantityImpl(0, UnitImpl.KILOMETER);
    
}
