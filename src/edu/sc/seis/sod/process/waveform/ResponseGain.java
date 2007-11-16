package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * Applys the overall gain from the response to the seismogram, converting the
 * units from counts to something physical, like m/s. This in NOT a
 * deconvolution, merely a constant multiplier. Created: Wed Nov 6 17:58:10 2002
 * 
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell </a>
 * @version $Id: ResponseGain.java 19405 2007-11-16 21:31:29Z crotwell $
 */
public class ResponseGain implements WaveformProcess, Threadable {

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult process(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        if(seismograms.length > 0) {
            ProxyNetworkAccess na = Start.getNetworkArm()
                    .getNetwork(channel.get_id().network_id);
            try {
                ChannelId chanId = channel.get_id();
                Time seisTime = seismograms[0].begin_time;
                Sensitivity sens = na.retrieve_sensitivity(chanId, seisTime);
                Unit recordedUnits = na.retrieve_initial_units(chanId, seisTime);
                for(int i = 0; i < seismograms.length; i++) {
                    out[i] = edu.sc.seis.fissuresUtil.bag.ResponseGain.apply(seismograms[i],
                                                                             sens,
                                                                             recordedUnits);
                } // end of for (int i=0; i<seismograms.length; i++)
                return new WaveformResult(out, new StringTreeLeaf(this, true));
            } catch(ChannelNotFound e) {
                // channel not found is thrown if there is no response for a
                // channel at a time
                return new WaveformResult(out,
                                          new StringTreeLeaf(this,
                                                             false,
                                                             "No instrumentation found for time "
                                                                     + seismograms[0].begin_time.date_time));
            } catch(InstrumentationInvalid e) {
                return new WaveformResult(out,
                                          new StringTreeLeaf(this,
                                                             false,
                                                             "Invalid instrumentation for "
                                                                     + ChannelIdUtil.toString(channel.get_id())));
            }
        }
        return new WaveformResult(true, seismograms, this);
    }
}// ResponseGainProcessor
