package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.subsetter.eventArm.*;
import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * sample xml
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
 */

public class LinearDistanceMagnitudeRange extends DistanceRange implements EventStationSubsetter {
    /**
     * Creates a new <code>LinearDistanceMagnitudeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public LinearDistanceMagnitudeRange (Element config) throws ConfigurationException{
	super(config);
	
	Element subElement = SodUtil.getElement(config, "magnitudeRange");
	magnitudeRange = (MagnitudeRange) SodUtil.load(subElement, "edu.sc.seis.sod.subsetter.eventArm");
	
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
	Origin origin = null;
	origin = eventAccess.get_preferred_origin();
	
	double actualDistance = SphericalCoords.distance(origin.my_location.latitude,
							 origin.my_location.longitude,
							 station.my_location.latitude,
							 station.my_location.longitude);
	if( actualDistance >= getMinDistance().value && actualDistance <= getMaxDistance().value) {
	    double resultantMagnitude = magnitudeRange.getMinMagnitude().value + (actualDistance - getMinDistance().value)*(double)(magnitudeRange.getMaxMagnitude().value - magnitudeRange.getMinMagnitude().value)/(getMinDistance().value - getMaxDistance().value);
	     if(origin.magnitudes[0].value >= resultantMagnitude) {
		return true;
	    } 
	} 
	return false;
	
    }

   private  MagnitudeRange magnitudeRange;

}// LinearDistanceMagnitudeRange
