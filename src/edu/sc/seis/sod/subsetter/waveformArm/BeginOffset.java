package edu.sc.seis.sod.subsetter.waveformArm;


import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * specifies the beginOffset
 *<pre>
 * 	&lt;beginOffset&gt;
 *			&lt;unit&gt;SECOND&lt;/unit&gt;
 *			&lt;value&gt;-120&lt;/value&gt;
 * 	&lt;/beginOffset&gt;
 *</pre>
 */		
 

public class BeginOffset extends Interval {
    /**
     * Creates a new <code>BeginOffset</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public BeginOffset (Element config){
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
    
}// BeginOffset
