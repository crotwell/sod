package edu.sc.seis.sod.subsetter.dataCenter;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.CookieJar;

/**
 * SeismogramDCLocator.java
 *
 *
 * Created: Thu Jul 25 16:19:09 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface SeismogramDCLocator {

    public ProxySeismogramDC getSeismogramDC(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters, CookieJar cookieJar) throws Exception;

}// SeismogramDCLocator
