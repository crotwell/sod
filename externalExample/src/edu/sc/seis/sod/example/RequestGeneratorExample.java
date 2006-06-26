package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;


public class RequestGeneratorExample implements RequestGenerator {

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel,
                                           CookieJar cookieJar)
            throws Exception {
        return new RequestFilter[0];
    }
}
