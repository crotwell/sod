package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * OriginDepthRange.java
 *
 *
 * Created: Tue Apr  2 13:34:59 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class OriginDepthRange extends edu.sc.seis.sod.subsetter.DepthRange implements OriginSubsetter{
    /**
     * Creates a new <code>OriginDepthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public OriginDepthRange (Element config){
	super(config);
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event, Origin origin, CookieJar cookies) {
	QuantityImpl actualDepth = (QuantityImpl)origin.my_location.depth;
	if(actualDepth.greaterThanEqual((QuantityImpl)getMinDepth()) && actualDepth.lessThanEqual((QuantityImpl)getMaxDepth())) {
	    return true;
	} else return false;

    }

  
}// OriginDepthRange
