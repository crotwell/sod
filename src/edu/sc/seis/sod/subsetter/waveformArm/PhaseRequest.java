package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;

/**
 * PhaseRequest.java
 *
 *
 * Created: Mon Apr  8 15:03:55 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PhaseRequest implements RequestGenerator{
    public PhaseRequest (Element config){
	
    }

    public RequestFilter[] generateRequest(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  CookieJar cookies){
	
	return null;

    }
    
    
}// PhaseRequest
