package edu.sc.seis.sod.subsetter.dataCenter;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.subsetter.AbstractSource;

public class FixedDataCenter extends AbstractSource implements SodElement,
        SeismogramDCLocator {

    public FixedDataCenter() {
        super("edu/iris/dmc", "IRIS_Datacenter");
        init();
    }
    
    public FixedDataCenter(Element element) {
        super(element);
        init();
    }

    public FixedDataCenter(String dns, String name) {
        super(dns, name);
        init();
    }
    
    void init() {
        dataCenter = BulletproofVestFactory.vestSeismogramDC(getDNS(),
                                                             getName(),
                                                             getFissuresNamingService(),
                                                             Start.createRetryStrategy(getRetries()));
    }

    public ProxySeismogramDC getSeismogramDC(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        return dataCenter;
    }

    public ProxySeismogramDC getDataCenter() {
        return dataCenter;
    }

    private ProxySeismogramDC dataCenter;

}// FixedDataCenter
