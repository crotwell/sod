package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * specifies the backAzimuth Range
 *<pre>
 * &lt;backAzimuthRange&gt;
 *      &lt;min&gt;30&lt;/min&gt;
 *      &lt;max&gt;180&lt;/max&gt;
 * &lt;/backAzimuthRange&gt;
 *</pre>
 */


public class BackAzimuthRange extends RangeSubsetter implements EventStationSubsetter {
    /**
     * Creates a new <code>BackAzimuthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public BackAzimuthRange (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations eventAccess,  NetworkAccess network,Station station, CookieJar cookies) throws Exception{
    float minValue = getMinValue();
    float maxValue = getMaxValue();
    if(minValue > 180) minValue = minValue - 360;
    if(maxValue > 180) maxValue = maxValue - 360;
    Origin origin = eventAccess.get_preferred_origin();
    double azimuth = SphericalCoords.azimuth(station.my_location.latitude,
                         station.my_location.longitude,
                         origin.my_location.latitude,
                         origin.my_location.longitude);

    if(azimuth >= minValue && azimuth <= maxValue) {
        return true;
    } else return false;
    }

}// BackAzimuthRange
