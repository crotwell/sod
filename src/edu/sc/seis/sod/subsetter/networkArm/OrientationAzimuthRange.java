package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class OrientationAzimuthRange 
    extends RangeSubsetter implements ChannelSubsetter {
    
    /**
     * Creates a new <code>OrientationAzimuthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public OrientationAzimuthRange(Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAccess network, Channel e,  CookieJar cookies) throws Exception{
	float minValue = getMinValue();
	float maxValue = getMaxValue();
	if(minValue > 180) minValue = minValue - 360;
	if(maxValue > 180) maxValue = maxValue - 360;

	if(e.an_orientation.azimuth >= minValue && e.an_orientation.azimuth <= maxValue) {
	    return true;
	} else return false;

    }

}// OrientationAzimuthRange
