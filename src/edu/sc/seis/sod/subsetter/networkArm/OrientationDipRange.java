package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class OrientationDipRange 
    extends RangeSubsetter implements ChannelSubsetter {
    
    /**
     * Creates a new <code>OrientationDipRange</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public OrientationDipRange (Element config) throws ConfigurationException {
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
	if(e.an_orientation.dip >= getMinValue() && e.an_orientation.dip <= getMaxValue()) {
	    return true;
	} else return false;
    }

}// OrientationDipRange
