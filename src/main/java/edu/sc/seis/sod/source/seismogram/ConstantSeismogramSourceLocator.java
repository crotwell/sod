package edu.sc.seis.sod.source.seismogram;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
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
                                                ChannelImpl channel,
                                                RequestFilter[] infilters,
                                                CookieJar cookieJar) throws Exception {
        return getSeismogramSource();
    }
    
    public abstract SeismogramSource getSeismogramSource();
}
