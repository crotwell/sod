package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;
import org.w3c.dom.*;

/**
 * This tag is used to specify the value of the catalog.
 *<pre>
 * &lt;contributor&gt;&lt;value&gt;NEIC&lt;/value&gt;&lt;/contributor&gt;
 *</pre>
 */


public class Contributor implements OriginSubsetter{
    /**
     * Creates a new <code>Contributor</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public Contributor (Element config){
	
	this.config = config;
    }
    
    /**
     * returns true if the contributor of the origin is same as the corresponding 
     * contributor specified in the configuration file.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event, Origin origin, CookieJar cookies) {
	if(origin.contributor.equals(getContributor())) return true;
	
	return false;
    }


    /**
     * returns the contributor.
     *
     * @return a <code>String</code> value
     */
    public String getContributor() {

	return SodUtil.getNestedText(config);
    }

    private Element config;
  
}// Contributor
