package edu.sc.seis.sod;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

/**
 * RequestGenerator.java
 *
 *
 * Created: Thu Dec 13 17:25:25 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface  RequestGenerator {

    public RequestFilter generateRequest(Event event, 
					 Channel channel, 
					 CookieJar cookies);
    
}// RequestGenerator
