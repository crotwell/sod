package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *networkIDAND contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 * 
 * sample xml file
 *<body><pre><bold>
 *	&lt;networkIDAND&gt;
 *		&lt;networkCode&gt;&lt;value&gt;II&lt;/value&gt;&lt;/networkCode&gt;
 *		&lt;networkCode&gt;&lt;value&gt;IU&lt;/value&gt;&lt;/networkCode&gt;
 *		&lt;networkCode&gt;&lt;value&gt;SP&lt;/value&gt;&lt;/networkCode&gt;
 *	&lt;/networkIDAND&gt;
 * </bold></pre></body>
 *
 */


public class NetworkIDAND 
    extends  NetworkLogicalSubsetter 
    implements NetworkIdSubsetter {
    
    public NetworkIDAND (Element config) throws ConfigurationException {
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

}// NetworkIDAND
