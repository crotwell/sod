package edu.sc.seis.sod.subsetter.request.vector;

import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannelGroup;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;


public class VectorRequestScript extends AbstractScriptSubsetter implements VectorRequestSubsetter {

    public VectorRequestScript(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, ChannelGroup channelGroup, RequestFilter[][] request, MeasurementStorage cookieJar)
            throws Exception {
        return runScript(new VelocityEvent(event),
                         new VelocityChannelGroup(channelGroup),
                         VelocityRequest.wrap(request, channelGroup),
                         cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityEvent event,
                                VelocityChannelGroup channelGroup,
                                List<List<VelocityRequest>> request,
                                MeasurementStorage cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channelGroup", channelGroup);
        engine.put("request", request);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
