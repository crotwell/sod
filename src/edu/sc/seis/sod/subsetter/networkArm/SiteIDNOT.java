package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *siteIDNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 *  
 * sample xml file
 *<body><pre><bold>
 *&lt;siteIDNOT&gt;
 *	&lt;siteCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/siteCode&gt;
 *&lt;/siteIDNOT&gt;
 * </bold></pre></body>
 */

public class SiteIDNOT 
    extends  NetworkLogicalSubsetter 
    implements SiteIdSubsetter {
    
    public SiteIDNOT (Element config) throws ConfigurationException {
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

}// SiteIDNOT
