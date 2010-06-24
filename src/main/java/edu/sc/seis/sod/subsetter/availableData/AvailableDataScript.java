package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class AvailableDataScript extends AbstractScriptSubsetter implements AvailableDataSubsetter {

    public AvailableDataScript(Element config) {
        super(config);
        // TODO Auto-generated constructor stub
    }

    @Override
    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channel", channel);
        engine.put("original", original);
        engine.put("available", available);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
