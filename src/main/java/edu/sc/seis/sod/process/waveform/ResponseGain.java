package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * Applys the overall gain from the response to the seismogram, converting the
 * units from counts to something physical, like m/s. This in NOT a
 * deconvolution, merely a constant multiplier. Created: Wed Nov 6 17:58:10 2002
 * 
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell </a>
 * @version $Id: ResponseGain.java 22061 2011-02-18 14:36:12Z crotwell $
 */
public class ResponseGain implements WaveformProcess, Threadable {

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        if(seismograms.length > 0) {
            NetworkSource na = Start.getNetworkArm().getNetworkSource();
            try {
                QuantityImpl sensitivity = na.getSensitivity(channel);
                if (sensitivity == null) {
                    throw new ChannelNotFound();
                }
                /*
                Instrumentation inst = na.getInstrumentation(chanId);
                if (inst == null) {throw new ChannelNotFound();}
                InstrumentationLoader.checkResponse(inst.the_response);
                Sensitivity sens = na.getSensitivity(chanId);
                Unit recordedUnits = inst.the_response.stages[0].input_units;
                 */
                for(int i = 0; i < seismograms.length; i++) {
                    out[i] = edu.sc.seis.sod.bag.ResponseGain.apply(seismograms[i],
                                                                             (float)sensitivity.getValue(),
                                                                             sensitivity.getUnit());
                } // end of for (int i=0; i<seismograms.length; i++)
                return new WaveformResult(out, new StringTreeLeaf(this, true));
            } catch(ChannelNotFound e) {
                // channel not found is thrown if there is no response for a
                // channel at a time
                return new WaveformResult(out,
                                          new StringTreeLeaf(this,
                                                             false,
                                                             "No instrumentation found for time "
                                                                     + TimeUtils.toISOString(seismograms[0].begin_time)));
            } catch(InvalidResponse e) {
                return new WaveformResult(out,
                                          new StringTreeLeaf(this,
                                                             false,
                                                             "Invalid instrumentation for "
                                                                     + ChannelIdUtil.toString(channel)+": "+e.getMessage()));
            }
        }
        return new WaveformResult(true, seismograms, this);
    }
}// ResponseGainProcessor
