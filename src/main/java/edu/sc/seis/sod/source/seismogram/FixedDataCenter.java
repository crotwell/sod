package edu.sc.seis.sod.source.seismogram;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.source.AbstractSource;

public class FixedDataCenter extends AbstractSource implements SodElement,
        SeismogramSourceLocator {

    public FixedDataCenter() {
        super("edu/iris/dmc", DEFAULT_SERVER_NAME);
        init();
    }
    
    public FixedDataCenter(Element element) {
        super(element, DEFAULT_SERVER_NAME);
        init();
    }

    public FixedDataCenter(String dns, String name) {
        super(dns, name);
        init();
    }
    
    void init() {
        dataCenter = new DataCenterSource(BulletproofVestFactory.vestSeismogramDC(getDNS(),
                                                             getName(),
                                                             getFissuresNamingService(),
                                                             Start.createRetryStrategy(getRetries())));
    }

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        return dataCenter;
    }

    public ProxySeismogramDC getDataCenter() {
        return dataCenter.getDataCenter();
    }

    private DataCenterSource dataCenter;
    
    public static final String DEFAULT_SERVER_NAME="IRIS_DataCenter";

}// FixedDataCenter