package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.*;

/**
 * LocalMotionVectorProcessor.java
 *
 *
 * Created: Thu Dec 13 18:11:22 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalMotionVectorProcess extends WaveFormArmProcess {


    public void process(EventAccessOperations event, 
			NetworkAccess network, 
			Channel[] channels, 
			RequestFilter[] original, 
			RequestFilter[] available,
			LocalMotionVector vector, 
			CookieJar cookies) throws Exception;
    
    
}// LocalMotionVectorProcessor
