package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class EventStationAND 
    extends  WaveFormLogicalSubsetter 
    implements EventStationSubsetter {
    
    public EventStationAND (Element config) throws ConfigurationException {
	super(config);
    }

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
