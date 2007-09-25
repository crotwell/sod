package edu.sc.seis.sod.source.network;

import java.util.regex.Pattern;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.cache.FilterNetworkDC;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;

/**
 * @author groves Created on Feb 4, 2005
 */
public class FilterNetworkFinder extends NetworkFinder {

    public FilterNetworkFinder(Element el) throws Exception {
        super(el);
        String url = SodUtil.nodeValueOfXPath(el, "filterURL/text()");
        patterns = FilterNetworkDC.readPattern(url);
    }

    public synchronized ProxyNetworkDC getNetworkDC() {
        if(netDC == null) {
            netDC = new FilterNetworkDC(new VestingNetworkDC(getDNS(),
                                                             getName(),
                                                             getFissuresNamingService(),
                                                             Start.createRetryStrategy()),
                                        patterns);
        }
        return netDC;
    }

    private Pattern[] patterns;

    private ProxyNetworkDC netDC;
}