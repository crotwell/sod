package edu.sc.seis.sod.subsetter.waveformArm;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import edu.sc.seis.sod.subsetter.eventArm.MagnitudeRange;

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

    public boolean accept(EventAccessOperations eventAccess,  Station station)
        throws Exception {
        Origin origin = null;
        origin = eventAccess.get_preferred_origin();
        double actualDistance = SphericalCoords.distance(origin.my_location.latitude,
                                                         origin.my_location.longitude,
                                                         station.my_location.latitude,
                                                         station.my_location.longitude);
        if( actualDistance >= getMinDistance().value && actualDistance <= getMaxDistance().value) {
            double resultantMagnitude = magnitudeRange.getMinValue() + (actualDistance - getMinDistance().value)*(double)(magnitudeRange.getMaxValue() - magnitudeRange.getMinValue())/(getMinDistance().value - getMaxDistance().value);
            if(origin.magnitudes[0].value >= resultantMagnitude) {
                return true;
            }
        }
        return false;
    }

    private  MagnitudeRange magnitudeRange;
}// LinearDistanceMagnitudeRange
