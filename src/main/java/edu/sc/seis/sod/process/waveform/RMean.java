package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * Removes the mean from the seismograms.
 * 
 * 
 * Created: Wed Nov 6 17:58:10 2002
 * 
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RMean.java 21405 2010-06-18 15:30:36Z crotwell $
 */
public class RMean implements WaveformProcess, Threadable {

    public boolean isThreadSafe() {
        return true;
    }

    /**
     * Removes the mean from the seismograms.
     */
    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for(int i = 0; i < seismograms.length; i++) {
            out[i] = rmean.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return new WaveformResult(out, new StringTreeLeaf(this, true));
    }

    private edu.sc.seis.sod.bag.RMean rmean = new edu.sc.seis.sod.bag.RMean();
}// RMean
