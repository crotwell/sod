package edu.sc.seis.sod.subsetter.request.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannelGroup;


public class VectorRequestScript extends AbstractScriptSubsetter implements VectorRequestSubsetter {

    public VectorRequestScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, ChannelGroup channelGroup, RequestFilter[][] request, CookieJar cookieJar)
            throws Exception {
        engine.put("event",  new VelocityEvent(event));
        engine.put("channelGroup",  new VelocityChannelGroup(channelGroup));
        engine.put("request", request);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
