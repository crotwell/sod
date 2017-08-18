package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * Removes the trend from the seismogram by subtracting the best least squares
 * line from the data.
 *
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RTrend.java 21405 2010-06-18 15:30:36Z crotwell $
 */

public class RTrend implements WaveformProcess, Threadable {

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
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

    edu.sc.seis.sod.bag.RTrend rtrend = new edu.sc.seis.sod.bag.RTrend();

}// ResponseGainProcessor
