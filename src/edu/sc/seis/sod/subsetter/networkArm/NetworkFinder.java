package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.NSNetworkDC;
import edu.sc.seis.fissuresUtil.cache.RetryNetworkDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.AbstractSource;
import org.w3c.dom.Element;


/**
 * This subsetter specifies the networkFinder.
 * <pre>
 *  &lt;networkFinder&gt;
 *      &lt;description&gt;Connect to the SCEPP networkDC&lt;/description&gt;
 *      &lt;name&gt;SCEPPNetworkDC&lt;/name&gt;
 *      &lt;dns&gt;edu/sc/seis&lt;/dns&gt;
 *      &lt;refreshInterval&gt;
 *          &lt;unit&gt;MINUTE&lt;/unit&gt;
 *          &lt;value&gt;30&lt;/value&gt;
 *      &lt;/refreshInterval&gt;
 *  &lt;/networkFinder&gt;
 *
 *                     (or)
 *
 *  &lt;networkFinder&gt;
 *      &lt;description&gt;Connect to the SCEPP networkDC&lt;/description&gt;
 *      &lt;name&gt;SCEPPNetworkDC&lt;/name&gt;
 *      &lt;dns&gt;edu/sc/seis&lt;/dns&gt;
 *  &lt;/networkFinder&gt;
 * </pre>
 */

public class NetworkFinder extends AbstractSource{

    public NetworkFinder (Element element) throws Exception{
        super(element);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        FissuresNamingService fns = commonAccess.getFissuresNamingService();

        String dns = getDNSName();
        String objectName = getSourceName();
        Element subElement = SodUtil.getElement(element,"refreshInterval");
        if(subElement != null) {
            refreshInterval = SodUtil.loadTimeInterval(subElement);
        } else refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        netDC = new RetryNetworkDC(new NSNetworkDC(dns, objectName, fns), 2);
    }

    public NetworkDCOperations getNetworkDC() throws Exception{
        return netDC;
    }

    public TimeInterval getRefreshInterval() {
        return this.refreshInterval;
    }

    private NetworkDCOperations netDC;

    private TimeInterval refreshInterval;

}// NetworkFinder
