package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * Removes the mean from the seismograms.
 * 
 * 
 * Created: Wed Nov 6 17:58:10 2002
 * 
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RMean.java 13876 2005-06-15 16:35:25Z groves $
 */
public class RMean implements WaveformProcess {

    /**
     * Removes the mean from the seismograms.
     */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for(int i = 0; i < seismograms.length; i++) {
            out[i] = rmean.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return new WaveformResult(out, new StringTreeLeaf(this, true));
    }

    private edu.sc.seis.fissuresUtil.bag.RMean rmean = new edu.sc.seis.fissuresUtil.bag.RMean();
}// RMean
