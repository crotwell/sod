package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * Describe class <code>NetworkCode</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NetworkCode implements NetworkIdSubsetter {

    /**
     * Creates a new <code>NetworkCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public NetworkCode(Element config) {
		System.out.println("The name of the element passed to NetworkCode is "+config.getTagName());
		System.out.println("The value of the Network Code is "+SodUtil.getNestedText(config));
		this.config = config;
	}

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e a <code>NetworkId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkId e, CookieJar cookies) {
		System.out.println("The network Code that is checked is "+e.network_code);
		if(e.network_code.equals(SodUtil.getNestedText(config))) return true;
		else return false;

	}

	private Element config = null;

}
