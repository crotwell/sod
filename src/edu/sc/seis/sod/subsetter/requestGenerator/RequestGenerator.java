package edu.sc.seis.sod.subsetter.requestGenerator;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * RequestGenerator.java
 *
 *
 * Created: Thu Dec 13 17:25:25 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface  RequestGenerator extends Subsetter{

    public RequestFilter[] generateRequest(CacheEvent event,
                                           Channel channel, CookieJar cookieJar) throws Exception;

}// RequestGenerator
