package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class EventStationAND 
    extends  WaveFormLogicalSubsetter 
    implements EventStationSubsetter {
    
    /**
     * Creates a new <code>EventStationAND</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventStationAND (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations o, NetworkAccess network, Station station,  CookieJar cookies) 
	throws Exception{
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventStationSubsetter filter = (EventStationSubsetter)it.next();
	    if (!filter.accept(o, network, station, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// EventStationAND
