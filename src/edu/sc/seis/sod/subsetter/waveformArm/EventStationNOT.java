package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class EventStationNOT 
    extends  NetworkLogicalSubsetter 
    implements EventStationSubsetter {
    
    public EventStationNOT (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(EventAccessOperations o, Station station,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    EventStationSubsetter filter = (EventStationSubsetter)it.next();
	    if ( filter.accept(o, station, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// EventStationNOT
