package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Transfer;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.sac.SacPoleZero;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeLeaf;


/**
 * @author crotwell
 * Created on Aug 1, 2005
 */
public class TransferResponse implements WaveformProcess, Threadable {

    public TransferResponse(Element config) throws ConfigurationException {
        lowCut = DOMHelper.extractFloat(config, "lowCut", DEFAULT_LOW_CUT);
        lowPass = DOMHelper.extractFloat(config, "lowPass", DEFAULT_LOW_PASS);
        highPass = DOMHelper.extractFloat(config, "highPass", DEFAULT_HIGH_PASS);
        highCut = DOMHelper.extractFloat(config, "highCut", DEFAULT_HIGH_CUT);
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        ProxyNetworkAccess na = Start.getNetworkArm()
                .getNetwork(channel.get_id().network_id);
        if(seismograms.length > 0) {
            try {
                ChannelId chanId = channel.get_id();
                Time seisTime = seismograms[0].begin_time;
                Instrumentation inst = na.retrieve_instrumentation(chanId, seisTime);
                SacPoleZero polezero = FissuresToSac.getPoleZero(inst.the_response);
                Transfer transfer = new Transfer();
                for(int i = 0; i < seismograms.length; i++) {
                    out[i] = transfer.apply(seismograms[i], polezero, lowCut, lowPass, highPass, highCut);
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
            } catch (InstrumentationInvalid e) {
                return new WaveformResult(out,
                                          new StringTreeLeaf(this,
                                                             false,
                                                             "Invalid instrumentation for "
                        + ChannelIdUtil.toString(channel.get_id())));
            }
        }
        return new WaveformResult(true, seismograms, this);
    }

    public boolean isThreadSafe() {
        return true;
    }
    
    float lowCut, lowPass, highPass, highCut;
    
    public static final float DEFAULT_LOW_CUT = -2f;
    public static final float DEFAULT_LOW_PASS = -2f;
    public static final float DEFAULT_HIGH_PASS = 1e5f;
    public static final float DEFAULT_HIGH_CUT = 1e6f;
}
