package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.*;

/**
 * MotionVectorFilter.java
 *
 *
 * Created: Thu Dec 13 17:59:58 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface MotionVectorSubsetter {

    public boolean accept(EventAccessOperations event, 
			  Channel channel, 
			  LocalMotionVector motionVectors,
			  CookieJar cookies);
    
}// MotionVectorFilter
