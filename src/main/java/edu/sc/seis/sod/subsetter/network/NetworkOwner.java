package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * NetworkAttrOwner.java
 * sample xmlfile
 * <pre>
 * &lt;networkAttrOwner&gt;&lt;value&gt;somename&lt;/value&gt;&lt;/networkAttrOwner&gt;
 * </pre>
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class NetworkOwner implements NetworkSubsetter {

    public NetworkOwner (Element config) throws ConfigurationException {
        this.config = config;
    }

    public StringTree accept(NetworkAttr net) {
        return new StringTreeLeaf(this, net.getOwner().equals(SodUtil.getNestedText(config)));
    }

    private Element config;
}// NetworkAttrOwner
