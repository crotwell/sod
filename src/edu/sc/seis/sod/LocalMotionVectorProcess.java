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


    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channels a <code>Channel[]</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param vector a <code>LocalMotionVector</code> value
     * @param cookies a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public void process(EventAccessOperations event, 
			NetworkAccess network, 
			Channel[] channels, 
			RequestFilter[] original, 
			RequestFilter[] available,
			LocalMotionVector vector, 
			CookieJar cookies) throws Exception;
    
    
}// LocalMotionVectorProcessor
