package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Filter;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Transfer;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.InstrumentationLoader;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.seisFile.sac.SacPoleZero;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
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

    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        try {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        NetworkSource na = Start.getNetworkArm().getNetworkSource();
        if(seismograms.length > 0) {

            SacPoleZero polezero = checkResponse(channel, na);
            Transfer transfer = new Transfer();
            for(int i = 0; i < seismograms.length; i++) {
                out[i] = transfer.apply(seismograms[i], polezero, lowCut, lowPass, highPass, highCut);
            } // end of for (int i=0; i<seismograms.length; i++)
            return new WaveformResult(out, new StringTreeLeaf(this, true));
        }
        return new WaveformResult(true, seismograms, this);
        } catch(InvalidResponse e) {
            return new WaveformResult(seismograms, new Fail(this, e.getMessage()));
        }
    }
    
    public static SacPoleZero checkResponse(ChannelImpl chan, NetworkSource na) throws InvalidResponse {
        try {
            Instrumentation inst = na.getInstrumentation(chan);
            InstrumentationLoader.checkResponse(inst.the_response);
            Filter filter = inst.the_response.stages[0].filters[0];
            if (filter.discriminator().value() != FilterType._POLEZERO) {
                String filterType = " ("+filter.discriminator().value()+")";
                if (filter.discriminator().value() == FilterType._COEFFICIENT) {
                    filterType = "Coefficient Response ("+filterType+")";
                } else if (filter.discriminator().value() == FilterType._LIST) {
                    filterType = "List Response ("+filterType+")";
                }
                throw new InvalidResponse("Instrumentation is not a of pole-zero type: "+filterType);
            }
            return FissuresToSac.getPoleZero(inst.the_response);
        } catch(ChannelNotFound e) {
            // channel not found is thrown if there is no response for a
            // channel at a time
            throw new InvalidResponse("No instrumentation found", e);
        }
    }

    public boolean isThreadSafe() {
        return true;
    }
    
    float lowCut, lowPass, highPass, highCut;
    
    public static final float DEFAULT_LOW_CUT = 0.005f;
    public static final float DEFAULT_LOW_PASS = 0.01f;
    public static final float DEFAULT_HIGH_PASS = 1e5f;
    public static final float DEFAULT_HIGH_CUT = 1e6f;
}
