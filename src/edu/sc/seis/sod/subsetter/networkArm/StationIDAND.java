package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * stationIDAND contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *  
 * sample xml file
 *<pre><bold>
 *&lt;stationIDAND&gt;
 *&lt;/stationIDAND&gt;
 * </bold></pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class StationIDAND 
    extends  NetworkLogicalSubsetter 
    implements StationIdSubsetter {
    
    /**
     * Creates a new <code>StationIDAND</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public StationIDAND (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e a <code>StationId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(StationId e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while(it.hasNext()) {
	    StationIdSubsetter filter = (StationIdSubsetter)it.next();
	    if ( !filter.accept(e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// StationIDAND
