package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class RequestScript extends AbstractScriptSubsetter implements RequestSubsetter {

    public RequestScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, ChannelImpl channel, RequestFilter[] request, CookieJar cookieJar)
            throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("request", request);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
