package edu.sc.seis.sod.subsetter.waveFormArm;
import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

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

public interface MotionVectorSubsetter extends Subsetter {

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channels a <code>Channel[]</code> value
     * @param motionVectors a <code>LocalMotionVector</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network,
			  Channel[] channels, 
			  LocalMotionVector motionVectors,
			  CookieJar cookies) throws Exception;
    
}// MotionVectorFilter
