package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import org.w3c.dom.Element;

/**
 * Applys the overall gain from the response to the seismogram, converting
 * the units from counts to something physical, like m/s. This in NOT a
 * deconvolution, merely a constant multiplier.
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: ResponseGainProcessor.java 7822 2004-03-25 20:34:37Z crotwell $
 */

public class ResponseGainProcessor implements LocalSeismogramProcess {
    public ResponseGainProcessor (Element config) {}

    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar)
        throws Exception {
        ResponseGain responseGain = new ResponseGain(Start.getNetworkArm().getFinder());
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i=0; i<seismograms.length; i++) {
            out[i] = responseGain.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return out;
    }
}// ResponseGainProcessor
