package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *stationXOR contains a sequence of channelSubsetters. The minimum value of the sequence is 2 and
 *the max value of the sequence is 2.
 *  
 * sample xml file
 *<body><pre><bold>
 *&lt;stationXOR&gt;
 *               &lt;stationArea&gt;
 *		    &lt;boxArea&gt;
 *			&lt;latitudeRange&gt;
 *				&lt;min&gt;20&lt;/min&gt;
 *				&lt;max&gt;40&lt;/max&gt;
 *			&lt;/latitudeRange&gt;
 *			&lt;longitudeRange&gt;
 *				&lt;min&gt;-100&lt;/min&gt;
 *				&lt;max&gt;-80&lt;/max&gt;
 *			&lt;/longitudeRange&gt;
 *		    &lt;/boxArea&gt;
 *		&lt;/stationArea&gt;
 *		&lt;stationeffectiveTimeOverlap&gt;
 *			&lt;effectiveTimeOverlap&gt;
 *				&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *				&lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *			&lt;/effectiveTimeOverlap&gt;
 *		&lt;/stationeffectiveTimeOverlap&gt;
 *&lt;/stationXOR&gt;
 * </bold></pre></body>
 */

public class StationXOR 
    extends  NetworkLogicalSubsetter 
    implements StationSubsetter {
    
    public StationXOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(NetworkAccess network, Station e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    StationSubsetter filter = (StationSubsetter)it.next();
	    if ( filter.accept(network, e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// StationXOR
