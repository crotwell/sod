package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;


/**
 * specifies the azimuth Range
 *<pre>
 * &lt;azimuthRange&gt;
 *      &lt;min&gt;30&lt;/min&gt;
 *      &lt;max&gt;180&lt;/max&gt;
 * &lt;/azimuthRange&gt;
 *</pre>
 */
     



public class AzimuthRange extends RangeSubsetter implements EventStationSubsetter {
    /**
     * Creates a new <code>AzimuthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public AzimuthRange (Element config){
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
    public boolean accept(EventAccessOperations eventAccess,  NetworkAccess network,Station station, CookieJar cookies) throws Exception {
    float minValue = getMinValue();
    float maxValue = getMaxValue();
    if(minValue > 180) minValue = minValue - 360;
    if(maxValue > 180) maxValue = maxValue - 360;
    Origin origin = eventAccess.get_preferred_origin();
    double azimuth = SphericalCoords.azimuth(origin.my_location.latitude,
                        origin.my_location.longitude,
                        station.my_location.latitude,
                        station.my_location.longitude);
    
    if(azimuth >= minValue && azimuth <= maxValue) {
        return true;
    } else return false;
    }


}// AzimuthRange
