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
    extends  NetworkLogicalSubsetter 
    implements EventStationSubsetter {
    
    public EventStationAND (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(EventAccessOperations o, Station station,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    ChannelSubsetter filter = (ChannelSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// EventStationAND
