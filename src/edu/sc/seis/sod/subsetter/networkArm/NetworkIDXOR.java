package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 *
 *networkIDXOR contains a sequence of channelSubsetters. The minimum value of the sequence is 2 and
 *the max value of the sequence is 2.
 * 
 * sample xml file
 *<body><pre><bold>
 *	&lt;networkIDXOR&gt;
 *		&lt;networkCode&gt;&lt;value&gt;II&lt;/value&gt;&lt;/networkCode&gt;
 *		&lt;networkCode&gt;&lt;value&gt;IU&lt;/value&gt;&lt;/networkCode&gt;
 *	&lt;/networkIDXOR&gt;
 * </bold></pre></body>
 *
 */

public class NetworkIDXOR 
    extends  NetworkLogicalSubsetter 
    implements NetworkIdSubsetter {
    
    public NetworkIDXOR (Element config) throws ConfigurationException {
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

}// NetworkIDXOR
