package edu.sc.seis.sod.source.seismogram;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.source.AbstractSource;


public abstract class ConstantSeismogramSourceLocator extends AbstractSource implements SeismogramSourceLocator {

    public ConstantSeismogramSourceLocator(Element config, String defaultName, int defaultRetries) {
        super(config, defaultName, defaultRetries);
    }

    public ConstantSeismogramSourceLocator(String name, int retries) {
        super(name, retries);
    }

    public ConstantSeismogramSourceLocator(Element config, String defaultName) {
        super(config, defaultName);
    }

    public ConstantSeismogramSourceLocator(String name) {
        super(name);
    }

    @Override
    public final SeismogramSource getSeismogramSource(CacheEvent event,
                                                Channel channel,
                                                RequestFilter[] infilters,
                                                CookieJar cookieJar) throws Exception {
        return getSeismogramSource();
    }
    
    public abstract SeismogramSource getSeismogramSource();
}
