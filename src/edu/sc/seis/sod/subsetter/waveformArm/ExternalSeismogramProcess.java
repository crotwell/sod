package edu.sc.seis.sod.subsetter.waveform;

import org.w3c.dom.*;
import edu.sc.seis.sod.*;

/**
 * ExternalSeismogramProcess.java
 *
 *
 * Created: Fri Apr 12 16:25:02 2002
 *
 * @author Philip Crotwell
 * @version $Id: ExternalSeismogramProcess.java 1602 2002-05-08 21:45:04Z crotwell $
 */

public class ExternalSeismogramProcess 
    extends ExternalProcess 
    implements LocalSeismogramProcess 
{
    public ExternalSeismogramProcess (Element config) 
	throws ConfigurationException {
	super(config);
    }

public void process(EventAccessOperations event,
                        NetworkAccess network,
                        Channel channel,
                        RequestFilter[] original,
                        RequestFilter[] available,
                        LocalSeismogram[] seismograms,
                        CookieJar cookies) {

}
    
}// ExternalSeismogramProcess
