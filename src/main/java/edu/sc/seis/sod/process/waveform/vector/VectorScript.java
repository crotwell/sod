package edu.sc.seis.sod.process.waveform.vector;

import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.subsetter.UnknownScriptResult;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannelGroup;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;
import edu.sc.seis.sod.velocity.seismogram.VelocitySeismogram;


public class VectorScript extends AbstractScriptSubsetter implements WaveformVectorProcess {

    public VectorScript(Element config) {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                       ChannelGroup channelGroup,
                                       RequestFilter[][] original,
                                       RequestFilter[][] available,
                                       LocalSeismogramImpl[][] seismograms,
                                       CookieJar cookieJar) throws Exception {
        return runScript(new VelocityEvent(event),
                         new VelocityChannelGroup(channelGroup),
                         VelocityRequest.wrap(original, channelGroup),
                         VelocityRequest.wrap(available, channelGroup),
                         VelocitySeismogram.wrap(seismograms, channelGroup),
                         cookieJar);
    }

    /** Run the script with the arguments as predefined variables.  */
    public WaveformVectorResult runScript(VelocityEvent event,
                                VelocityChannelGroup channelGroup,
                                List<List<VelocityRequest>> request,
                                List<List<VelocityRequest>> available,
                                List<List<VelocitySeismogram>> seismograms,
                                CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channelGroup);
        engine.put("request", request);
        engine.put("available", available);
        engine.put("seismograms", seismograms);
        engine.put("cookieJar", cookieJar);
        // seismogram process return WaveformVectorResult instead of StringTree, so we can't simply use super.eval()
        Object result = preeval();
        if (result == null) {
            // try getting variable named result from engine
            result = engine.get("result");
        }
        if (result == null) {
            // assume all well and return a Pass
            return new WaveformVectorResult((LocalSeismogramImpl[][])engine.get("seismograms"), new Pass(this));
        }
        if (result instanceof WaveformVectorResult) {
            return (WaveformVectorResult)result;
        } else if (result instanceof StringTree) {
            return new WaveformVectorResult((LocalSeismogramImpl[][])engine.get("seismograms"), (StringTree)result);
        } else if (result instanceof Boolean) {
            return new WaveformVectorResult((LocalSeismogramImpl[][])engine.get("seismograms"), new StringTreeLeaf(this, ((Boolean)result).booleanValue()));
        } else {

            throw new UnknownScriptResult("Script returns unknown results type, should be boolean or StringTree or WaveformVectorResult: " + result.toString());
        }
    }
}
