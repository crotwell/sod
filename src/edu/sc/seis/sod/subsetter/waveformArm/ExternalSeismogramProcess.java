package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;
import edu.sc.seis.sod.*;

/**
 * ExternalSeismogramProcess.java
 *
 *
 * Created: Fri Apr 12 16:25:02 2002
 *
 * @author Philip Crotwell
 * @version $Id: ExternalSeismogramProcess.java 2880 2002-11-06 20:26:35Z crotwell $
 */

public class ExternalSeismogramProcess 
    implements LocalSeismogramProcess 
{
    public ExternalSeismogramProcess (Element config) 
	throws ConfigurationException {
	externalProcess = (LocalSeismogramProcess) SodUtil.loadExternal(config);
	
    }

    public LocalSeismogram[] process(EventAccessOperations event,
				     NetworkAccess network,
				     Channel channel,
				     RequestFilter[] original,
				     RequestFilter[] available,
				     LocalSeismogram[] seismograms,
				     CookieJar cookies) throws Exception{
	return externalProcess.process(event, 
				       network, 
				       channel, 
				       original, 
				       available, 
				       seismograms, 
				       cookies);
    }

    LocalSeismogramProcess externalProcess;
    
}// ExternalSeismogramProcess
