package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * EndPhase.java
 *
 *
 * Created: Tue Apr  9 10:19:32 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EndPhase implements Subsetter{
    /**
     * Creates a new <code>EndPhase</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EndPhase (Element config){
	this.config = config;
    }

    public String getPhase() {

	return SodUtil.getNestedText(config);
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
    
    private Element config;
}// EndPhase
