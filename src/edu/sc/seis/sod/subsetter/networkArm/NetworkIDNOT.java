package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *networkIDNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 * 
 * sample xml file
 *<body><pre><bold>
 *	&lt;networkIDNOT&gt;
 *		&lt;networkCode&gt;&lt;value&gt;II&lt;/value&gt;&lt;/networkCode&gt;
 *	&lt;/networkIDNOT&gt;
 * </bold></pre></body>
 *
 */

public class NetworkIDNOT 
    extends  NetworkLogicalSubsetter 
    implements NetworkIdSubsetter {
    
    public NetworkIDNOT (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(NetworkId e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    NetworkIdSubsetter filter = (NetworkIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// NetworkIDNOT
