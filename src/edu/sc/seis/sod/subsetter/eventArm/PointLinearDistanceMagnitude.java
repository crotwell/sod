/**
 * LinearDistanceMagnitudeRange.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.waveformArm.LinearDistanceMagnitudeRange;
import org.w3c.dom.Element;

public class PointLinearDistanceMagnitude extends LinearDistanceMagnitudeRange implements OriginSubsetter {

    public PointLinearDistanceMagnitude(Element element) throws ConfigurationException {
        super(element);
        double[] latlon = AbstractOriginPoint.getLatLon(element);
        lat = latlon[0];
        lon = latlon[1];
    }

   public boolean accept(EventAccessOperations eventAccess, EventAttr eventAttr, Origin preferred_origin)
        throws Exception {
        return accept(eventAccess, lat, lon);
    }


    private double lat;

    private double lon;
}

