package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.bag.ChannelSeismogram;
import edu.sc.seis.sod.bag.FlippedChannel;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class InvertFlippedChannels extends ForkProcess {

    public InvertFlippedChannels(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] fixedSeis = new LocalSeismogramImpl[seismograms.length];
        Channel fixedChan = channel;
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
