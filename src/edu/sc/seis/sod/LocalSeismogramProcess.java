package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;
/**
 * LocalSeismogramProcess.java
 *
 *
 * Created: Thu Dec 13 18:03:03 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalSeismogramProcess extends WaveFormArmProcess {

    public void process(EventAccessOperations event, 
			NetworkAccessOperations network, 
			Channel channel, 
			RequestFilter[] original, 
			RequestFilter[] available,
			LocalSeismogram[] seismograms, 
			CookieJar cookies) throws Exception;
    
}// LocalSeismogramProcessor
