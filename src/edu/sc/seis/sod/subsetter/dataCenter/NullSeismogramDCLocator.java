package edu.sc.seis.sod.subsetter.dataCenter;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;

/**
 * NullSeismogramDCLocator.java
 * 
 * 
 * Created: Wed Apr 2 11:52:13 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NullSeismogramDCLocator implements SodElement, SeismogramDCLocator {

    public ProxySeismogramDC getSeismogramDC(CacheEvent event,
                                             Channel channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        throw new ConfigurationException("Cannot use NullSeismogramDCLocator to get a seismogramDC. There must be another type of SeismogramDCLocator within the configuration script");
    }
} // NullSeismogramDCLocator
