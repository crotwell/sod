package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *siteNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 *  
 * sample xml file
 *<body><pre><bold>
 *&lt;siteNOT&gt;
 *&lt;/siteNOT&gt;
 * </bold></pre></body>
 */

public class SiteNOT 
    extends  NetworkLogicalSubsetter 
    implements SiteSubsetter {
    
    public SiteNOT (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(Site e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    SiteSubsetter filter = (SiteSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// SiteNOT
