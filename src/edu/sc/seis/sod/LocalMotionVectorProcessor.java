package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.util.*;

/**
 * LocalMotionVectorProcessor.java
 *
 *
 * Created: Thu Dec 13 18:11:22 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalMotionVectorProcessor {


    public void process(Event event, 
			Channel channel, 
			MotionVector[] vectors, 
			CookieJar cookies);
    
    
}// LocalMotionVectorProcessor
