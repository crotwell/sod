package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * specifies the networkCode.
 * <pre>
 * &lt;networkCode&gt;&lt;value&gt;SP&lt;/value&gt;&lt;/networkCode&gt;
 * </pre>
 */
public class NetworkCode implements NetworkSubsetter {

    /**
     * Creates a new <code>NetworkCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public NetworkCode(Element config) {
        this.config = config;
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event a <code>NetworkAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAttr attr, CookieJar cookies) throws Exception {
        if(attr.get_code().equals(SodUtil.getNestedText(config))) return true;
        else return false;

    }

    private Element config = null;

}
