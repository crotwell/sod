package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import org.w3c.dom.*;


/**
 * NullRequestSubsetter.java
 *
 *
 * Created: Wed Mar 19 16:16:50 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NullRequestSubsetter implements RequestSubsetter {
    public NullRequestSubsetter() {
        
    } // NullRequestSubsetter constructor

    public NullRequestSubsetter(Element config) {
        
    } // NullRequestSubsetter constructor
    
    public boolean accept(EventAccessOperations event, 
                          NetworkAccess network, 
                          Channel channel, 
                          RequestFilter[] request,
                          CookieJar cookies) 
        throws Exception 
    {
        return true;
    }

} // NullRequestSubsetter
