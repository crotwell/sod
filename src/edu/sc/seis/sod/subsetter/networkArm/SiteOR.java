package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *siteOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *  
 * sample xml file
 *<body><pre><bold>
 *&lt;siteOR&gt;
 *&lt;/siteOR&gt;
 * </bold></pre></body>
 */


public class SiteOR 
    extends  NetworkLogicalSubsetter 
    implements SiteSubsetter {
    
    public SiteOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(NetworkAccess network, Site e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    SiteSubsetter filter = (SiteSubsetter)it.next();
	    if ( filter.accept(network, e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// SiteOR
