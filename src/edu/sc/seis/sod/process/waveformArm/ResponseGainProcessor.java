package edu.sc.seis.sod.process.waveFormArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import edu.sc.seis.fissuresUtil.bag.*;
import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * Applys the overall gain from the response to the seismogram, converting
 * the units from counts to something physical, like m/s. This in NOT a
 * deconvolution, merely a constant multiplier.
 *
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: ResponseGainProcessor.java 7180 2004-02-17 22:21:52Z groves $
 */

public class ResponseGainProcessor implements LocalSeismogramProcess {

    /**
     * Creates a new <code>ResponseGain</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public ResponseGainProcessor (Element config) {
	this.config = config;
    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param seismograms a <code>LocalSeismogram[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public LocalSeismogram[] process(EventAccessOperations event, 
				     NetworkAccess network, 
				     Channel channel, 
				     RequestFilter[] original, 
				     RequestFilter[] available,
				     LocalSeismogram[] seismograms, 
				     CookieJar cookies) throws Exception {
	ResponseGain responseGain = new ResponseGain(network);
	LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
	for (int i=0; i<seismograms.length; i++) {
	    out[i] = responseGain.apply((LocalSeismogramImpl)seismograms[i]);
	} // end of for (int i=0; i<seismograms.length; i++)
	return out;
    }

    Element config;

}// ResponseGainProcessor
