package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

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

public class NetworkAttrOwner implements NetworkAttrSubsetter {
    
    /**
     * Creates a new <code>NetworkAttrOwner</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public NetworkAttrOwner (Element config) throws ConfigurationException {
	this.config = config;
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e an <code>NetworkAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAttr e,  CookieJar cookies) {
	if(e.owner.equals(SodUtil.getNestedText(config))) return true;
	else return false;
    }

    private Element config;
}// NetworkAttrOwner
