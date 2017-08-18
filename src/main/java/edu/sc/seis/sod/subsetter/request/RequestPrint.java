/**
 * RequestPrint.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

@Deprecated
public class RequestPrint implements RequestSubsetter {

    public RequestPrint() {
        System.err.println("Deprecated: Please use <printlineRequest> instead.");
    }
    
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
                    + ChannelIdUtil.toStringNoDates(request[i].channel_id) + " from "
                    + request[i].start_time.getISOTime() + " to "
                    + request[i].end_time.getISOTime());
        }
    }
    
    
}