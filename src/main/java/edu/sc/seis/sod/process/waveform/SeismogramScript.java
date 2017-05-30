package edu.sc.seis.sod.process.waveform;

import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.subsetter.UnknownScriptResult;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;
import edu.sc.seis.sod.velocity.seismogram.VelocitySeismogram;


public class SeismogramScript extends AbstractScriptSubsetter implements WaveformProcess {

    public SeismogramScript(Element config) {
        super(config);
    }

    public WaveformResult accept(CacheEvent event,
                                 ChannelImpl channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws Exception {
        return runScript(new VelocityEvent(event),
                         new VelocityChannel(channel),
                         VelocityRequest.wrap(original, channel),
                         VelocityRequest.wrap(available, channel),
                         VelocitySeismogram.wrap(seismograms, channel),
                         cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public WaveformResult runScript(VelocityEvent event,
                                VelocityChannel channel,
                                List<VelocityRequest> request,
                                List<VelocityRequest> available,
                                List<VelocitySeismogram> seismograms,
                                CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("request", request);
        engine.put("available", available);
        engine.put("seismograms", seismograms);
        engine.put("cookieJar", cookieJar);
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
