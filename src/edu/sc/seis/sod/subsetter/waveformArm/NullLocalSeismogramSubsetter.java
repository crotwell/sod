package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;
/**
 * NullLocalSeismogramSubsetter.java
 *
 *
 * Created: Fri Apr 12 13:41:05 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class NullLocalSeismogramSubsetter implements LocalSeismogramSubsetter{
    public NullLocalSeismogramSubsetter (){
	
    }
    
    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  RequestFilter[] original, 
			  RequestFilter[] available,
			  LocalSeismogram[] seismograms, 
			  CookieJar cookies) throws Exception {
	
	return true;

    }
        
}// LocalSeismogram
