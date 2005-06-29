package edu.sc.seis.sod.subsetter.network;

import java.util.regex.Pattern;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.FilterNetworkDC;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.sod.SodUtil;

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
            netDC = BulletproofVestFactory.vestNetworkDC(getDNS(),
                                                         getName(),
                                                         getFissuresNamingService());
            netDC = new FilterNetworkDC(netDC, patterns);
        }
        return netDC;
    }

    private Pattern[] patterns;

    private ProxyNetworkDC netDC;
}