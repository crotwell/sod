package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * siteIDOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *  
 * sample xml file
 *<pre><bold>
 *&lt;siteIDOR&gt;
 *	&lt;siteCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/siteCode&gt;
 *	&lt;siteCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/siteCode&gt;
 *&lt;/siteIDOR&gt;
 * </bold></pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class SiteIDOR 
    extends  NetworkLogicalSubsetter 
    implements SiteIdSubsetter {
    
    /**
     * Creates a new <code>SiteIDOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public SiteIDOR (Element config) throws ConfigurationException{
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e a <code>SiteId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(SiteId e,  CookieJar cookies) throws Exception{
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
