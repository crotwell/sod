package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class SeismogramScript extends AbstractScriptSubsetter implements WaveformProcess {

    public SeismogramScript(Element config) {
        super(config);
        // TODO Auto-generated constructor stub
    }

    @Override
    public WaveformResult accept(CacheEvent event,
                                 ChannelImpl channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("original", original);
        engine.put("available", available);
        engine.put("seismograms", seismograms);
        engine.put("cookieJar", cookieJar);
        StringTree result = eval();
        return new WaveformResult((LocalSeismogramImpl[])engine.get("seismograms"), result);
    }
}
