package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;


public class EventChannelScript extends AbstractScriptSubsetter implements EventChannelSubsetter {

    public EventChannelScript(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, Channel channel, MeasurementStorage cookieJar) throws Exception {
        return runScript(new VelocityEvent(event), new VelocityChannel(channel), cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityEvent event,  VelocityChannel channel, MeasurementStorage cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
