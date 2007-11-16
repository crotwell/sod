/**
 * LinearDistanceMagnitudeRange.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.eventStation.LinearDistanceMagnitudeRange;
import org.w3c.dom.Element;

public class PointLinearDistanceMagnitude extends LinearDistanceMagnitudeRange implements OriginSubsetter {

    public PointLinearDistanceMagnitude(Element element) throws ConfigurationException {
        super(element);
        double[] latlon = AbstractOriginPoint.getLatLon(element, "pointLinearDistanceMagnitude");
        lat = latlon[0];
        lon = latlon[1];
    }

   public StringTree accept(CacheEvent eventAccess, EventAttr eventAttr, Origin preferred_origin)
        throws Exception {
        return new StringTreeLeaf(this, accept(eventAccess, lat, lon));
    }


    private double lat;

    private double lon;
}

