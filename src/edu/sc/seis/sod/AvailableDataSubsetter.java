package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * AvailableDataSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:18:32 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface AvailableDataSubsetter {

    public boolean accept(EventAccessOperations event, Station station, CookieJar cookies);
    
}// AvailableDataSubsetter
