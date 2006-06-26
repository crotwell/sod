package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequest;


public class VectorRequestExample implements VectorRequest {

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar) throws Exception {
        return false;
    }
}
