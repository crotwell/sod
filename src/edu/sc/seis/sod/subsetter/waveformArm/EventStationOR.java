package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class EventStationOR 
    extends  WaveFormLogicalSubsetter 
    implements EventStationSubsetter {
    
    public EventStationOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(EventAccessOperations o, Station station,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while(it.hasNext()) {
	    EventStationSubsetter filter = (EventStationSubsetter)it.next();
	    if ( filter.accept(o, station, cookies)) {
		return true;
	    }
	}
	return false;
    }

}// EventStationOR
