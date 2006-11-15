package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.UserReportRetryStrategy;
import edu.sc.seis.sod.subsetter.AbstractSource;

public class NetworkFinder extends AbstractSource {

    public NetworkFinder(Element element) throws Exception {
        super(element);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        fns = commonAccess.getFissuresNamingService();
        Element subElement = SodUtil.getElement(element, "refreshInterval");
        if(subElement != null) {
            refreshInterval = SodUtil.loadTimeInterval(subElement);
        } else {
            refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        }
    }

    public synchronized ProxyNetworkDC getNetworkDC() {
        if(netDC == null) {
            netDC = new VestingNetworkDC(getDNS(),
                                         getName(),
                                         getFissuresNamingService(),
                                         new UserReportRetryStrategy(),
                                         -1);
        }
        return netDC;
    }

    public TimeInterval getRefreshInterval() {
        return this.refreshInterval;
    }

    public FissuresNamingService getFissuresNamingService() {
        return fns;
    }

    private FissuresNamingService fns;

    private VestingNetworkDC netDC;

    private TimeInterval refreshInterval;
}// NetworkFinder
