package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;


public class SacHeaderScript extends AbstractScriptSubsetter implements SacProcess {

    public SacHeaderScript(Element config) {
        super(config);
    }

    public void process(SacTimeSeries sac, CacheEvent event, ChannelImpl channel) throws Exception {
        engine.put("event",  new VelocityEvent(event));
        engine.put("channel",  new VelocityChannel(channel));
        engine.put("sac",  sac);
        // don't care about result as script's only purpose is to modify sac file
        preeval();
    }
    
}
