package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;

/**
 * LocalSeismogramSubsetter.java
 *
 *
 * Created: Thu Dec 13 18:01:05 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalSeismogramSubsetter {
    
    public boolean accept(EventAccessOperations event, 
			  NetworkAccessOperations network, 
			  Channel channel, 
			  RequestFilter[] original, 
			  RequestFilter[] available,
			  LocalSeismogram[] seismograms, 
			  CookieJar cookies);

    
}// LocalSeismogramSubsetter
