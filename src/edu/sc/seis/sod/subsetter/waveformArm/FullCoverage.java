package edu.sc.seis.sod.subsetter.waveFormArm;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.subsetter.waveFormArm.AvailableDataSubsetter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;

/**
 * sample xml
 *<pre>
 *&lt;fullCoverage/&gt;
 *</pre>
 */

public class FullCoverage implements AvailableDataSubsetter, SodElement{
    /**
     * Creates a new <code>FullCoverage</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public FullCoverage (Element config){
	
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
	if (original.length == available.length) {
	    return true;
	} // end of if (original.length == available.length)
	logger.debug("FAIL fullCoverage");	
	return false;
    }

    static Category logger = 
	Category.getInstance(FullCoverage.class.getName());

}// FullCoverage
