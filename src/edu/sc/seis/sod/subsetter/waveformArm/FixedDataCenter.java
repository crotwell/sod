package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.AbstractSource;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SeismogramDCLocator;
import edu.sc.seis.sod.SodElement;
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

public class FixedDataCenter
    extends AbstractSource
    implements SodElement, SeismogramDCLocator
{

    /**
     * Creates a new <code>FixedDataCenter</code> instance.
     *
     * @param element an <code>Element</code> value
     */
    public FixedDataCenter (Element element) throws Exception{
    super(element);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        fissuresNamingService = commonAccess.getFissuresNamingService();

        dns = getDNSName();
        objectName = getSourceName();
        dataCenter = new NSSeismogramDC(dns, objectName, fissuresNamingService);
    }

    /**
     * Describe <code>getSeismogramDC</code> method here.
     *
     * @return a <code>DataCenter</code> value
     */
    public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                      NetworkAccess network,
                                      Station station,
                                      CookieJar cookies) throws Exception{
        return dataCenter;
    }

    private ProxySeismogramDC dataCenter;

   private FissuresNamingService fissuresNamingService = null;

   private String dns = null;

   private String objectName = null;

}// FixedDataCenter
