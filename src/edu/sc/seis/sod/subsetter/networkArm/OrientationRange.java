package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.TauP.*;

import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class OrientationRange 
        implements ChannelSubsetter {
    
    /**
     * Creates a new <code>OrientationRange</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public OrientationRange (Element config) throws ConfigurationException {
	
	azimuth = Float.parseFloat(SodUtil.getNestedText(SodUtil.getElement(config, "azimuth")));
	dip = Float.parseFloat(SodUtil.getNestedText(SodUtil.getElement(config, "dip")));	
	offset = Float.parseFloat(SodUtil.getNestedText(SodUtil.getElement(config, "maxOffset")));       

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
	double actualDistance = SphericalCoords.distance(e.an_orientation.dip,
							 e.an_orientation.azimuth,
							 dip,
							 azimuth);
	if(actualDistance <= offset) {
	    return true;
	} else return false;
    }
    
    private float azimuth;

    private float dip;

    private float offset;

}// OrientationRange
