package edu.sc.seis.sod.subsetter.waveFormArm;


import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * EndOffset.java
 *
 *
 * Created: Mon Apr  8 16:41:31 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EndOffset extends Interval {
    /**
     * Creates a new <code>EndOffset</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EndOffset (Element config){
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  CookieJar cookies){


	return true;

    }
    
}// EndOffset
