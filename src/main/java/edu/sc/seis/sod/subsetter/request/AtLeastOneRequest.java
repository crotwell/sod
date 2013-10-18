package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequestSubsetter;

public class AtLeastOneRequest implements RequestSubsetter, VectorRequestSubsetter {

    public AtLeastOneRequest() {} 

    public AtLeastOneRequest(Element config) {}

    public StringTree accept(CacheEvent event,
                          ChannelImpl channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception {
        if (request.length > 0) {
            return new Pass(this);
        } else {
            return new Fail(this, "No requests");
        }
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        int total = 0;
        for (int i = 0; i < request.length; i++) {
            total += request[i].length;
        }
        if (total > 0 ) {
            return new Pass(this);
        } else {
            return new Fail(this, "No requests");
        }
    }
}