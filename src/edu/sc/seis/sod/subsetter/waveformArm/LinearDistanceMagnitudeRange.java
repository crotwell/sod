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
 * LinearDistanceMagnitudeRange.java
 *
 *
 * Created: Mon Apr  8 16:32:56 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
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
	    if(origin.magnitudes[0].value >= magnitudeRange.getMinMagnitude().value &&
	       origin.magnitudes[0].value <= magnitudeRange.getMaxMagnitude().value) {
		return true;
	    } 
	} 
	return false;
	
    }

   private  MagnitudeRange magnitudeRange;

}// LinearDistanceMagnitudeRange
