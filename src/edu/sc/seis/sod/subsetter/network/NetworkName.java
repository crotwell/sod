package edu.sc.seis.sod.subsetter.network;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * NetworkAttrName.java
 * sample xml file
 * <pre>
 * &lt;networkAttrName&gt;&lt;value&gt;somename*lt;/value&gt;&lt;/networkAttrName&gt;
 * </pre>
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class NetworkName implements NetworkSubsetter {

    public NetworkName (Element config) throws ConfigurationException {
        this.config = config;
    }

    public boolean accept(NetworkAttr net) {
        if(net.name.equals(SodUtil.getNestedText(config))) return true;
        else return false;
    }

    private Element config;
}// NetworkAttrName
