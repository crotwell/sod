package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * EventStationSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:18:32 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventStationSubsetter {

    public boolean accept(EventAccessOperations event, 
			  NetworkAccessOperations network, 
			  Station station, 
			  CookieJar cookies);
    
}// EventStationSubsetter
