package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.request.Request;


public class RequestExample implements Request {

    public boolean accept(CacheEvent event,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception {
        return false;
    }
}
