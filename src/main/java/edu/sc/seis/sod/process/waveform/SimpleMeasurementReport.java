package edu.sc.seis.sod.process.waveform;

import java.io.IOException;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;


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
