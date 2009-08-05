/**
 * RequestPrint.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class RequestPrint implements Request {

    public StringTree accept(CacheEvent event,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception {
        printRequests(request);
        return new Pass(this);
    }

    public static void printRequests(RequestFilter[] request) {
        for(int i = 0; i < request.length; i++) {
            System.out.println("Request "
                    + ChannelIdUtil.toString(request[i].channel_id) + " from "
                    + request[i].start_time.date_time + " to "
                    + request[i].end_time.date_time);
        }
    }
}