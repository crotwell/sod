package edu.sc.seis.sod.subsetter.dataCenter;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
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

    public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                             Channel channel,
                                             RequestFilter[] infilters, CookieJar cookieJar) throws Exception;

}// SeismogramDCLocator
