package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * SiteDepthRange.java
 *
 *
 * Created: Tue Apr  2 13:34:59 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class SiteDepthRange extends edu.sc.seis.sod.subsetter.UnitRange implements SiteSubsetter{
    /**
     * Creates a new <code>SiteDepthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SiteDepthRange (Element config){
	super(config);
	System.out.println("IN DEPTH RANGE minimum depth is "+getUnitRange().min_value);
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param network an <code>NetworkAccess</code> value
     * @param station an <code>Site</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Site site, CookieJar cookies) {
	double actualDepth = site.my_location.depth.value;
	if(actualDepth >= getMinDepth().value && actualDepth <= getMaxDepth().value) {
	    return true;
	} else return false;

    }

    /**
     * Describe <code>getMinDepth</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public Quantity getMinDepth() {
	System.out.println("minimum depth is "+getUnitRange().min_value);
	return new QuantityImpl(getUnitRange().min_value, getUnitRange().the_units);
    }

    /**
     * Describe <code>getMaxDepth</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public Quantity getMaxDepth() {
	System.out.println("maximum depth is "+getUnitRange().max_value);
	return new QuantityImpl(getUnitRange().max_value, getUnitRange().the_units);
    }
  
}// SiteDepthRange
