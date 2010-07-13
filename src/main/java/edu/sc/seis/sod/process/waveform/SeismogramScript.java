package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.subsetter.UnknownScriptResult;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;


public class SeismogramScript extends AbstractScriptSubsetter implements WaveformProcess {

    public SeismogramScript(Element config) {
        super(config);
    }

    @Override
    public WaveformResult accept(CacheEvent event,
                                 ChannelImpl channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws Exception {
        engine.put("event",  new VelocityEvent(event));
        engine.put("channel",  new VelocityChannel(channel));
        engine.put("request", original);
        engine.put("available", available);
        engine.put("seismograms", seismograms);
        engine.put("cookieJar", cookieJar);
        engine.eval("import bag");
        // seismogram process return WaveformResult instead of StringTree, so we can't simply use super.eval()
        Object result = preeval();
        if (result == null) {
            // try getting variable named result from engine
            result = engine.get("result");
        }
        if (result == null) {
            // assume all well and return a Pass
            return new WaveformResult((LocalSeismogramImpl[])engine.get("seismograms"), new Pass(this));
        }
        if (result instanceof WaveformResult) {
            return (WaveformResult)result;
        } else if (result instanceof StringTree) {
            return new WaveformResult((LocalSeismogramImpl[])engine.get("seismograms"), (StringTree)result);
        } else if (result instanceof Boolean) {
            return new WaveformResult((LocalSeismogramImpl[])engine.get("seismograms"), new StringTreeLeaf(this, ((Boolean)result).booleanValue()));
        } else {
            throw new UnknownScriptResult("Script returns unknown results type, should be boolean or StringTree or WaveformResult: " + result.toString());
        }
    }
}
