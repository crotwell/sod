package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * NetworkAttrName.java
 * sample xml file
 * <pre>
 * &lt;networkAttrName&gt;&lt;value&gt;somename*lt;/value&gt;&lt;/networkAttrName&gt;
 * </pre>
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author Philip Crotwell
 */

public class NetworkName implements NetworkSubsetter {

    public NetworkName (Element config) throws ConfigurationException {
        this.config = config;
    }

    public StringTree accept(NetworkAttrImpl net) {
        return new StringTreeLeaf(this, net.getName().equals(SodUtil.getNestedText(config)));
    }

    private Element config;
    
}// NetworkAttrName
