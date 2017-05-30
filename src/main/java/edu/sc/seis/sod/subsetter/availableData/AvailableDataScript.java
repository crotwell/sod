package edu.sc.seis.sod.subsetter.availableData;

import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;


public class AvailableDataScript extends AbstractScriptSubsetter implements AvailableDataSubsetter {

    public AvailableDataScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) throws Exception {
        return runScript(new VelocityEvent(event),
                         new VelocityChannel(channel),
                         VelocityRequest.wrap(request, channel),
                         VelocityRequest.wrap(available, channel),
                         cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityEvent event,
                                VelocityChannel channel,
                                List<VelocityRequest> request,
                                List<VelocityRequest> available,
                                CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("request", request);
        engine.put("available", available);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
