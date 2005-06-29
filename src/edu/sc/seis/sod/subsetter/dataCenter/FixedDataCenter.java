package edu.sc.seis.sod.subsetter.dataCenter;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.subsetter.AbstractSource;
import org.w3c.dom.Element;

/**
 * FixedDataCenter.java Created: Wed Mar 20 14:27:42 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
public class FixedDataCenter extends AbstractSource implements SodElement,
        SeismogramDCLocator {

    public FixedDataCenter(Element element) throws Exception {
        super(element);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        fissuresNamingService = commonAccess.getFissuresNamingService();
        dns = getDNS();
        objectName = getName();
        dataCenter = BulletproofVestFactory.vestSeismogramDC(dns,
                                                             objectName,
                                                             fissuresNamingService);
    }

    public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                             Channel channel,
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

    private String dns = null;

    private String objectName = null;
}// FixedDataCenter
