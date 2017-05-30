package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequestSubsetter;

public class PassRequest implements RequestSubsetter, VectorRequestSubsetter {

    public PassRequest() {} 

    public PassRequest(Element config) {}

    public StringTree accept(CacheEvent event,
                          ChannelImpl channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception {
        return new Pass(this);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        return new Pass(this);
    }
}
