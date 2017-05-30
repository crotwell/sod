package edu.sc.seis.sod.process.waveform;

import java.io.IOException;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;


public class SimpleMeasurementReport extends PrintlineSeismogramProcess implements WaveformProcess {

    public SimpleMeasurementReport(Element config) throws ConfigurationException {
        super(config);
    }
    
    @Override
    public WaveformResult accept(CacheEvent event,
                                 ChannelImpl channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws IOException {
        return super.accept(event, channel, original, available, seismograms, cookieJar);
    }
}
