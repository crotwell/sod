package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Location;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import edu.sc.seis.sod.subsetter.eventArm.MagnitudeRange;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import org.w3c.dom.Element;

/**
 * sample xml
 *<pre>
 *  &lt;linearDistanceMagnitude&gt;
 *    &lt;magnitudeRange&gt;
 *           &lt;description&gt;describes magnitude&lt;/description&gt;
 *           &lt;magType&gt;mb&lt;/magType&gt;
 *           &lt;min&gt;5.5&lt;/min&gt;
 *     &lt;/magnitudeRange&gt;
 *     &lt;distanceRange&gt;
 *           &lt;unit&gt;DEGREE&lt;/unit&gt;
 *           &lt;min&gt;30&lt;/min&gt;
 *     &lt;/distanceRange&gt;
 *  &lt;/linearDistanceMagnitude&gt;
 *</pre>
 */

public class LinearDistanceMagnitudeRange extends DistanceRangeSubsetter implements EventStationSubsetter {
    public LinearDistanceMagnitudeRange (Element config) throws ConfigurationException{
        super(config);
        Element subElement = SodUtil.getElement(config, "magnitudeRange");
        magnitudeRange = (MagnitudeRange) SodUtil.load(subElement, "eventArm");
    }

    public boolean accept(EventAccessOperations eventAccess,  Station station, CookieJar cookieJar)
        throws Exception {
        Origin origin = eventAccess.get_preferred_origin();
        Location originLoc = origin.my_location;
        Location stationLoc = station.my_location;
        double actualDistance = SphericalCoords.distance(originLoc.latitude,
                                                         originLoc.longitude,
                                                         stationLoc.latitude,
                                                         stationLoc.longitude);
        if( actualDistance >= getMin().value && actualDistance <= getMax().value) {
            double resultantMagnitude = magnitudeRange.getMinValue() + (actualDistance - getMin().value)*(double)(magnitudeRange.getMaxValue() - magnitudeRange.getMinValue())/(getMin().value - getMax().value);
            if(origin.magnitudes[0].value >= resultantMagnitude) {
                return true;
            }
        }
        return false;
    }

    private  MagnitudeRange magnitudeRange;
}// LinearDistanceMagnitudeRange
