package edu.sc.seis.sod.process.waveFormArm;
import edu.sc.seis.sod.subsetter.waveFormArm.*;

import edu.sc.seis.sod.*;
import edu.sc.seis.fissuresUtil.bag.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * Cuts seismograms relative to phases.
 *
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:crotwell@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: PhaseCut.java 7180 2004-02-17 22:21:52Z groves $
 */

public class PhaseCut implements LocalSeismogramProcess {

    /**
     * Creates a new <code>PhaseCut</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public PhaseCut (Element config) throws ConfigurationException {
	this.config = config;
	// use existing PhaseRequest class to calculate phase times
	phaseRequest = new PhaseRequest(config);
    }

    /**
     * Cuts the seismograms based on phase arrivals.
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
	LocalSeismogramImpl[] out = 
	    new LocalSeismogramImpl[seismograms.length];
	RequestFilter[] cutRequest = 
	    phaseRequest.generateRequest(event, network, channel, cookies);
	logger.debug("Cutting from "+cutRequest[0].start_time.date_time+" to "+cutRequest[0].end_time.date_time);
	Cut cut = new Cut(new MicroSecondDate(cutRequest[0].start_time), 
			  new MicroSecondDate(cutRequest[0].end_time));
	for (int i=0; i<seismograms.length; i++) {
	    out[i] = cut.apply((LocalSeismogramImpl)seismograms[i]);
	} // end of for (int i=0; i<seismograms.length; i++)
	return out;
    }

    Element config;

    PhaseRequest phaseRequest;

    static Category logger = 
	Category.getInstance(PhaseRequest.class.getName());

}// PhaseCut
