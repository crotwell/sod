package edu.sc.seis.sod.source.network;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.sod.Start;

public class NetworkFinder extends NetworkSource {

    public NetworkFinder(Element element) throws Exception {
        super(element);
    }

    public synchronized ProxyNetworkDC getNetworkDC() {
        if(netDC == null) {
            netDC = new VestingNetworkDC(getDNS(),
                                         getName(),
                                         getFissuresNamingService(),
                                         Start.createRetryStrategy(getRetries()));
        }
        return netDC;
    }

    private VestingNetworkDC netDC;
}// NetworkFinder
