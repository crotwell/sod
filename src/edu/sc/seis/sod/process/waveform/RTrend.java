package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

/**
 * Removes the trend from the seismogram by subtracting the best least squares
 * line from the data.
 *
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RTrend.java 10413 2004-09-09 18:40:30Z groves $
 */

public class RTrend implements WaveformProcess {

    public RTrend (Element config) {
        this.config = config;
        rtrend = new edu.sc.seis.fissuresUtil.bag.RTrend();
    }

    /**
     * Removes the mean from the seismograms.
     */
    public WaveformResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i=0; i<seismograms.length; i++) {
            out[i] = rtrend.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return new WaveformResult(out, new StringTreeLeaf(this, true));
    }

    Element config;

    edu.sc.seis.fissuresUtil.bag.RTrend rtrend;

}// ResponseGainProcessor
