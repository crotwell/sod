package edu.sc.seis.sod.subsetter.requestGenerator;

import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;


public class RequestGeneratorScript extends AbstractScriptSubsetter implements RequestGenerator {

    public RequestGeneratorScript(Element config) {
        super(config);
    }

    public RequestFilter[] generateRequest(CacheEvent event, ChannelImpl channel, CookieJar cookieJar) throws Exception {
        return runScript(new VelocityEvent(event),
                         new VelocityChannel(channel),
                         cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public RequestFilter[] runScript(VelocityEvent event,
                                VelocityChannel channel,
                                CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("cookieJar", cookieJar);
        Object result = preeval();
        if (result == null) {
            // try getting variable named result from engine
            result = engine.get("result");
        }
        if (result == null) {
            throw new Exception("Script had a null result");
        }
        if (result instanceof RequestFilter[]) {
            return (RequestFilter[])result;
        } else if (result instanceof List) {
            return (RequestFilter[])((List)result).toArray(new RequestFilter[0]);
        }
        throw new Exception("Script did not return RequestFilter array or List: "+result.getClass().getName());
    }
    
    
}
