package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.IfNetwork.*;


import org.w3c.dom.*;
import org.apache.log4j.*;


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
    /**
     * Creates a new <code>NetworkFinder</code> instance.
     *
     * @param element an <code>Element</code> value
     */
    public NetworkFinder (Element element) throws Exception{
    super(element);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        fissuresNamingService = commonAccess.getFissuresNamingService();

        dns = getDNSName();
        objectName = getSourceName();
        Element subElement = SodUtil.getElement(element,"refreshInterval");
        if(subElement != null) {
            Object obj = SodUtil.load(subElement, "edu.sc.seis.sod.subsetter");
            refreshInterval = (RefreshInterval)obj;
        } else refreshInterval = null;

    }

    /**
     * Describe <code>getNetworkDC</code> method here.
     *
     * @return a <code>NetworkDC</code> value
     */
    public NetworkDC getNetworkDC() throws Exception{

        return fissuresNamingService.getNetworkDC(dns, objectName);

    }

    public RefreshInterval getRefreshInterval() {

    return this.refreshInterval;
    }

    private FissuresNamingService fissuresNamingService = null;

    private String dns = null;

    private String objectName = null;

    private RefreshInterval refreshInterval;

}// NetworkFinder
