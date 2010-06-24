package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class VectorScript extends AbstractScriptSubsetter implements WaveformVectorProcess {

    public VectorScript(Element config) {
        super(config);
    }

    @Override
    public WaveformVectorResult accept(CacheEvent event,
                                       ChannelGroup channelGroup,
                                       RequestFilter[][] original,
                                       RequestFilter[][] available,
                                       LocalSeismogramImpl[][] seismograms,
                                       CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channelGroup", channelGroup);
        engine.put("original", original);
        engine.put("available", available);
        engine.put("seismograms", seismograms);
        engine.put("cookieJar", cookieJar);
        StringTree result = eval();
        return new WaveformVectorResult((LocalSeismogramImpl[][])engine.get("seismograms"), result);
    }
}
