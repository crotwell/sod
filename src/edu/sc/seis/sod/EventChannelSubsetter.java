package edu.sc.seis.sod;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * EventChannelSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:19:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventChannelSubsetter {

    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  CookieJar cookies) throws Exception;
    
}// EventChannelSubsetter
