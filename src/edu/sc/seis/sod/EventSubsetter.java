package edu.sc.seis.sod;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

/**
 * EventFilter.java
 *
 *
 * Created: Thu Dec 13 17:03:44 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventSubsetter {

    public boolean accept(EventAccessOperations event, CookieJar cookies);

    
}// EventFilter
