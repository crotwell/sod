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
 * specifies the endOffset
 * 	&lt;endOffset&gt;
 *			&lt;unit&gt;SECOND&lt;/unit&gt;
 *			&lt;value&gt;-120&lt;/value&gt;
 * 	&lt;/endOffset&gt;
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
