package edu.sc.seis.sod.subsetter.requestGenerator;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * RequestGenerator.java
 *
 *
 * Created: Thu Dec 13 17:25:25 2001
 *
 * @author Philip Crotwell
 * @version
 */

public interface  RequestGenerator extends Subsetter{

    public RequestFilter[] generateRequest(CacheEvent event,
                                           Channel channel, CookieJar cookieJar) throws Exception;

}// RequestGenerator
