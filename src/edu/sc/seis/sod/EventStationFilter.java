package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * EventStationFilter.java
 *
 *
 * Created: Thu Dec 13 17:18:32 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventStationFilter {

    public boolean accept(Event event, Station station, CookieJar cookies);
    
}// EventStationFilter
