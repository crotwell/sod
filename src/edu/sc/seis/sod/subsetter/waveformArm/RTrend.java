package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * Removes the trend from the seismogram by subtracting the best least squares
 * line from the data.
 *
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RTrend.java 2924 2002-11-18 14:24:29Z crotwell $
 */

public class RTrend implements LocalSeismogramProcess {

    /**
     * Creates a new <code>RTrend</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public RTrend (Element config) {
	this.config = config;
	rtrend = new edu.sc.seis.fissuresUtil.bag.RTrend();
    }

    /**
     * Removes the mean from the seismograms.
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
	LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
	for (int i=0; i<seismograms.length; i++) {
	    out[i] = rtrend.apply((LocalSeismogramImpl)seismograms[i]);
	} // end of for (int i=0; i<seismograms.length; i++)
	return out;
    }

    Element config;

    edu.sc.seis.fissuresUtil.bag.RTrend rtrend;

}// ResponseGainProcessor
