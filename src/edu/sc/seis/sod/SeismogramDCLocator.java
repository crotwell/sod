package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;

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
                                      NetworkAccess network,
                                      Station station,
                                      CookieJar cookies) throws Exception;

}// SeismogramDCLocator
