package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *networkAttrNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 * 
 * sample xml file
 *<body><pre><bold>
 * &lt;networkAttrNOT&gt;
 *	&lt;networkeffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *			&lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *              &lt;/effectiveTimeOverlap&gt;
 *	&lt;/networkeffectiveTimeOverlap&gt;
 * &lt;/networkAttrNOT&gt;
 * </bold></pre></body>
 *
 */

public class NetworkAttrNOT 
    extends  NetworkLogicalSubsetter 
    implements NetworkAttrSubsetter {
    
    public NetworkAttrNOT (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(NetworkAttr e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    NetworkAttrSubsetter filter = (NetworkAttrSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// NetworkAttrNOT
