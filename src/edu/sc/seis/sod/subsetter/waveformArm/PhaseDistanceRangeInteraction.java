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
 * PhaseDistanceRangeInteraction.java
 *
 *
 * Created: Mon Apr  8 16:32:56 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PhaseDistanceRangeInteraction implements EventStationSubsetter {
    /**
     * Creates a new <code>PhaseDistanceRangeInteraction</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PhaseDistanceRangeInteraction (Element config){
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations eventAccess,  NetworkAccess network,Station station, CookieJar cookies) {
	return true;
    }

}// PhaseDistanceRangeInteraction
