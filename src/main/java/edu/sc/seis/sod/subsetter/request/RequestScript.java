package edu.sc.seis.sod.subsetter.request;

import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;


public class RequestScript extends AbstractScriptSubsetter implements RequestSubsetter {

    public RequestScript(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, Channel channel, RequestFilter[] request, MeasurementStorage cookieJar)
            throws Exception {
        return runScript(new VelocityEvent(event),
                         new VelocityChannel(channel),
                         VelocityRequest.wrap(request, channel),
                         cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityEvent event,
                                VelocityChannel channel,
                                List<VelocityRequest> request,
                                MeasurementStorage cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("request", request);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
