package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * DepthRange.java
 *
 *
 * Created: Tue Apr  2 13:34:59 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class DepthRange extends edu.sc.seis.sod.subsetter.UnitRange implements OriginSubsetter{
    public DepthRange (Element config){
	super(config);
	
    }
    
    public boolean accept(Origin origin, CookieJar cookies) {
	double actualDepth = origin.my_location.depth.value;
	if(actualDepth >= getMinDepth().value && actualDepth <= getMaxDepth().value) {
	    return true;
	} else return false;

    }

    public Quantity getMinDepth() {
	System.out.println("minimum depth is "+getUnitRange().min_value);
	return new QuantityImpl(getUnitRange().min_value, getUnitRange().the_units);
    }

    public Quantity getMaxDepth() {
	System.out.println("maximum depth is "+getUnitRange().max_value);
	return new QuantityImpl(getUnitRange().max_value, getUnitRange().the_units);
    }
  
}// DepthRange
