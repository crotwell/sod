package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.sc.seis.TauP.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;


/**
 * sample xml 
 *<pre>
 *  &lt;eventStationDistance&gt;
 *        &lt;distanceRange&gt;
 *              &lt;unit&gt;DEGREE&lt;/unit&gt;
 *              &lt;min&gt;30&lt;/min&gt;
 *        &lt;/distanceRange&gt;
 *  &lt;/eventStationDistance&gt;
 *</pre>
 */


public class EventStationDistance extends DistanceRange implements EventStationSubsetter {
    /**
     * Creates a new <code>EventStationDistance</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EventStationDistance (Element config) throws ConfigurationException{
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
    public boolean accept(EventAccessOperations eventAccess,  NetworkAccess network,Station station, CookieJar cookies) 
  	throws Exception  {
	Origin origin = null;
	    origin = eventAccess.get_preferred_origin();
	double actualDistance = SphericalCoords.distance(origin.my_location.latitude,
							 origin.my_location.longitude,
							 station.my_location.latitude,
							 station.my_location.longitude);
	if( actualDistance >= getMinDistance().value && actualDistance <= getMaxDistance().value) {
	    return true;
	} else return false;
    }

}// EventStationDistance
