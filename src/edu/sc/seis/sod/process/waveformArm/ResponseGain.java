package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

/**
 * Applys the overall gain from the response to the seismogram, converting
 * the units from counts to something physical, like m/s. This in NOT a
 * deconvolution, merely a constant multiplier.
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: ResponseGain.java 9541 2004-07-09 16:14:01Z groves $
 */

public class ResponseGain implements LocalSeismogramProcess {
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar)
        throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        NetworkAccess na  = Start.getNetworkArm().getNetwork(channel.get_id().network_id);
        if(seismograms.length > 0){
            Instrumentation inst = na.retrieve_instrumentation(channel.get_id(), seismograms[0].begin_time);
            for (int i=0; i<seismograms.length; i++) {
                out[i] = edu.sc.seis.fissuresUtil.bag.ResponseGain.apply(seismograms[i], inst);
            } // end of for (int i=0; i<seismograms.length; i++)
            return new LocalSeismogramResult(true, out, new StringTreeLeaf(this, true));
        }else{
            return new LocalSeismogramResult(true, seismograms);
        }
    }
}// ResponseGainProcessor
