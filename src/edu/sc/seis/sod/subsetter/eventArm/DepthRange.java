package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
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

	return true;

    }
    
  
}// DepthRange
