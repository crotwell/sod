package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

public class NetworkCode implements NetworkIdSubsetter,SodElement {

	public NetworkCode(Element config) {
		System.out.println("The name of the element passed to NetworkCode is "+config.getTagName());
		System.out.println("The value of the Network Code is "+SodUtil.getNestedText(config));
		this.config = config;
	}

	public boolean accept(NetworkId e, CookieJar cookies) {
		if(e.network_code.equals(SodUtil.getNestedText(config))) return true;
		else return false;

	}

	private Element config = null;

}
