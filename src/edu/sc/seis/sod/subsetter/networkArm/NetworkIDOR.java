package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 *
 *networkIDOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 * 
 * sample xml file
 *<body><pre><bold>
 *	&lt;networkIDOR&gt;
 *		&lt;networkCode&gt;&lt;value&gt;II&lt;/value&gt;&lt;/networkCode&gt;
 *		&lt;networkCode&gt;&lt;value&gt;IU&lt;/value&gt;&lt;/networkCode&gt;
 *		&lt;networkCode&gt;&lt;value&gt;SP&lt;/value&gt;&lt;/networkCode&gt;
 *	&lt;/networkIDOR&gt;
 * </bold></pre></body>
 *
 */

public class NetworkIDOR 
    extends  NetworkLogicalSubsetter 
    implements NetworkIdSubsetter {
    
    public NetworkIDOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(NetworkId e,  CookieJar cookies) {
	System.out.println("THe networkID to be checked in NetworkIDOR is "+e.network_code);
	System.out.println("The size of the list is "+ filterList.size());
	Iterator it = filterList.iterator();
	while(it.hasNext()) {
	    System.out.println("In while loop in accept method of NetworkIDOR");
	    NetworkIdSubsetter filter = (NetworkIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return true;
	    }
	}
	return false;
    }

}// NetworkIDOR
