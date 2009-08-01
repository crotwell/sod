package edu.sc.seis.sod.subsetter.dataCenter;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.subsetter.AbstractSource;

/**
 * FixedDataCenter.java Created: Wed Mar 20 14:27:42 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
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
        fissuresNamingService = CommonAccess.getNameService();
        dataCenter = BulletproofVestFactory.vestSeismogramDC(getDNS(),
                                                             getName(),
                                                             fissuresNamingService,
                                                             BulletproofVestFactory.getDefaultNumRetry(),
                                                             Start.createRetryStrategy());
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

    private FissuresNamingService fissuresNamingService = null;

}// FixedDataCenter
