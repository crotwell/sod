package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * siteNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 *  
 * sample xml file
 *<body><pre><bold>
 *&lt;siteNOT&gt;
 *  &lt;siteDepthRange&gt;
 *      &lt;unitRange&gt;
 *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *           &lt;min&gt;10&lt;/min&gt;
 *           &lt;max&gt;100&lt;/max&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/siteDepthRange&gt;
 *&lt;/siteNOT&gt;
 * </bold></pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class SiteNOT 
    extends  NetworkLogicalSubsetter 
    implements SiteSubsetter {
    
    /**
     * Creates a new <code>SiteNOT</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public SiteNOT (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Site</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Site e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    SiteSubsetter filter = (SiteSubsetter)it.next();
	    if ( filter.accept(network, e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// SiteNOT
