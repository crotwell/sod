package edu.sc.seis.sod.source.seismogram;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;

public class NullSeismogramDCLocator implements SodElement, SeismogramSourceLocator {

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        throw new ConfigurationException("Cannot use NullSeismogramDCLocator to get a seismogramDC. There must be another type of SeismogramDCLocator within the configuration script");
    }
} // NullSeismogramDCLocator
