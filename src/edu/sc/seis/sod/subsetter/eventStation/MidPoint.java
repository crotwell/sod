/**
 * MidPoint.java
 *
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.PointDistanceArea;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MidPoint implements EventStationSubsetter {

    public MidPoint(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                area = (edu.iris.Fissures.Area)SodUtil.load((Element)node,
                                                            "origin");
                break;
            }
        }
    }

    public StringTree accept(EventAccessOperations eventAccess,
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
        if(area instanceof edu.iris.Fissures.BoxArea) {
            edu.iris.Fissures.BoxArea boxArea = (edu.iris.Fissures.BoxArea)area;
            if(latitude >= boxArea.min_latitude
                    && latitude <= boxArea.max_latitude
                    && longitude >= boxArea.min_longitude
                    && longitude <= boxArea.max_longitude) {
                return new StringTreeLeaf(this, true);
            } else return new StringTreeLeaf(this, false);
        } else if(area instanceof GlobalArea) {
            return new StringTreeLeaf(this, true);
        } else if(area instanceof PointDistanceArea) {
            PointDistanceArea pDist = (PointDistanceArea)area;
            pDist.min_distance = QuantityImpl.createQuantityImpl(pDist.min_distance)
                    .convertTo(UnitImpl.DEGREE);
            pDist.max_distance = QuantityImpl.createQuantityImpl(pDist.max_distance)
                    .convertTo(UnitImpl.DEGREE);
            double midDist = SphericalCoords.distance(latitude,
                                                      longitude,
                                                      pDist.latitude,
                                                      pDist.longitude);
            if(midDist >= pDist.min_distance.value
                    && midDist <= pDist.max_distance.value) {
                return new StringTreeLeaf(this, true);
            } else {
                return new StringTreeLeaf(this, false);
            }
        }
        throw new Exception("Unknown Area, class=" + area.getClass());
    }

    Area area;
}
