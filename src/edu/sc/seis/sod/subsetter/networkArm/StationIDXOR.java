package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *stationIDXOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *  
 * sample xml file
 *<body><pre><bold>
 *&lt;stationIDXOR&gt;
 *&lt;/stationIDXOR&gt;
 * </bold></pre></body>
 */


public class StationIDXOR 
    extends  NetworkLogicalSubsetter 
    implements StationIdSubsetter {
    
    public StationIDXOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(StationId e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    StationIdSubsetter filter = (StationIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// StationIDXOR
