package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *siteIDOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *  
 * sample xml file
 *<body><pre><bold>
 *&lt;siteIDOR&gt;
 *	&lt;siteCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/siteCode&gt;
 *	&lt;siteCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/siteCode&gt;
 *&lt;/siteIDOR&gt;
 * </bold></pre></body>
 */


public class SiteIDOR 
    extends  NetworkLogicalSubsetter 
    implements SiteIdSubsetter {
    
    public SiteIDOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(SiteId e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    SiteIdSubsetter filter = (SiteIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// SiteIDOR
