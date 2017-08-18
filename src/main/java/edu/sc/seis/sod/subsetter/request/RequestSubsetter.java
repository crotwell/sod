package edu.sc.seis.sod.subsetter.request;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * RequestSubsetter.java Created: Wed Mar 19 15:56:24 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public interface RequestSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] request,
                             CookieJar cookieJar) throws Exception;
}// RequestSubsetter
