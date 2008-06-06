package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.ChannelSeismogram;
import edu.sc.seis.fissuresUtil.bag.FlippedChannel;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class InvertFlippedChannels extends ForkProcess {

    public InvertFlippedChannels(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] fixedSeis = new LocalSeismogramImpl[seismograms.length];
        ChannelImpl fixedChan = channel;
        for(int i = 0; i < seismograms.length; i++) {
            ChannelSeismogram corrected = FlippedChannel.correct(channel,
                                                                 seismograms[i]);
            fixedSeis[i] = (LocalSeismogramImpl)corrected.getSeismogram();
            fixedChan = corrected.getChannel();
        }
        return new WaveformResult(seismograms, doAND(event,
                                                     fixedChan,
                                                     original,
                                                     available,
                                                     fixedSeis,
                                                     cookieJar).getReason());
    }
}
