package edu.sc.seis.sod;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

/**
 * EventAttrSubsetter.java
 *
 * Created: Thu Dec 13 17:03:44 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventAttrSubsetter {

    public boolean accept(EventAttr event, CookieJar cookies);

    
}// EventSubsetter
