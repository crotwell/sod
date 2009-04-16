package edu.sc.seis.sod.source.network;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;

public class NetworkFinder extends NetworkSource {

    public NetworkFinder(Element element) throws Exception {
        super(element);
        fns = CommonAccess.getNameService();
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
                                         Start.createRetryStrategy(),
                                         -1);
        }
        return netDC;
    }

    public FissuresNamingService getFissuresNamingService() {
        return fns;
    }

    private FissuresNamingService fns;

    private VestingNetworkDC netDC;
}// NetworkFinder
