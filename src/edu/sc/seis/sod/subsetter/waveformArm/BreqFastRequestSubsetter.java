package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import org.w3c.dom.*;

/**
 * BreqFastRequestSubsetter.java
 *
 *
 * Created: Wed Mar 19 14:07:16 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class BreqFastRequestSubsetter implements RequestSubsetter {
    public BreqFastRequestSubsetter(Element config) {
        breqfast = new BreqFastAvailableData(config);
    }
    
    public boolean accept(EventAccessOperations event,
                          NetworkAccess network,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookies)throws Exception {
        return breqfast.accept(event,
                               network,
                               channel,
                               request,
                               request,
                               cookies);
    }
    BreqFastAvailableData breqfast;
} // BreqFastRequestGenerator
