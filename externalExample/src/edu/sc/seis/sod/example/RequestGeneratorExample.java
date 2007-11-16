package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;


public class RequestGeneratorExample implements RequestGenerator {

    public RequestFilter[] generateRequest(CacheEvent event,
                                           Channel channel,
                                           CookieJar cookieJar)
            throws Exception {
        return new RequestFilter[0];
    }
}
