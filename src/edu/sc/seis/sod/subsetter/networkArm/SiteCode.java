package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * 
 * sample xml file
 * &lt;siteCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/siteCode&gt;
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class SiteCode implements SiteIdSubsetter {

    /**
     * Creates a new <code>SiteCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SiteCode(Element config) {
	    this.config = config;
	
	}

    /**
     * Describe <code>accept</code> method here.
     *
     * @param siteId a <code>SiteId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(SiteId siteId, CookieJar cookies) {
	    if(siteId.site_code.equals(SodUtil.getNestedText(config))) return true;
	    else return false;

	}

    private Element config = null;


}
