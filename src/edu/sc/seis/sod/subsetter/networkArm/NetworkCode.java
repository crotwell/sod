package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * specifies the networkCode.
 * <pre>
 * &lt;networkCode&gt;&lt;value&gt;SP&lt;/value&gt;&lt;/networkCode&gt;
 * </pre>
 */
public class NetworkCode implements NetworkSubsetter {

    public NetworkCode(Element config) { this.config = config; }

    public boolean accept(NetworkAttr attr) throws Exception {
        if(attr.get_code().equals(SodUtil.getNestedText(config))) return true;
        else return false;

    }

    private Element config = null;
}
