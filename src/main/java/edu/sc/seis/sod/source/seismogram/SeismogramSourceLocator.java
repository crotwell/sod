package edu.sc.seis.sod.source.seismogram;

import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;


public interface SeismogramSourceLocator extends SodElement {

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters, CookieJar cookieJar) throws Exception;

}
