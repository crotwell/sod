package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.fissuresUtil.cache.RetryDataCenter;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.subsetter.AbstractSource;
import edu.sc.seis.sod.subsetter.waveformArm.SeismogramDCLocator;
import org.w3c.dom.Element;
/**
 * FixedDataCenter.java
 *
 *
 * Created: Wed Mar 20 14:27:42 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class FixedDataCenter extends AbstractSource implements SodElement,
    SeismogramDCLocator {

    public FixedDataCenter (Element element) throws Exception{
        super(element);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        fissuresNamingService = commonAccess.getFissuresNamingService();

        dns = getDNSName();
        objectName = getSourceName();
        dataCenter = new RetryDataCenter(new NSSeismogramDC(dns, objectName, fissuresNamingService), 2);
    }

    public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                             Channel channel,
                                             RequestFilter[] infilters, CookieJar cookieJar) throws Exception{
        return dataCenter;
    }

    private ProxySeismogramDC dataCenter;

    private FissuresNamingService fissuresNamingService = null;

    private String dns = null;

    private String objectName = null;

}// FixedDataCenter
