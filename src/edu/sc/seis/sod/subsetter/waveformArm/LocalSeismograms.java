package edu.sc.seis.sod.subsetter.waveFormArm;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramSubsetter;
/**
 * LocalSeismogram.java
 *
 *
 * Created: Fri Apr 12 13:41:05 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class LocalSeismograms implements LocalSeismogramSubsetter{
    public LocalSeismograms (Element config){
	
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
