package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;

/**
 * sample xml
 *<pre>
 *&lt;someCoverage/&gt;
 *</pre>
 */

public class SomeCoverage implements AvailableDataSubsetter, SodElement{
    /**
     * Creates a new <code>SomeCoverage</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SomeCoverage (Element config){
	
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  RequestFilter[] original, 
			  RequestFilter[] available, 
			  CookieJar cookies) {
	// simple impl, probably need more robust
	if (available != null && available.length != 0) {
	    return true;
	}
	
	return false;
    }
    
}// SomeCoverage
