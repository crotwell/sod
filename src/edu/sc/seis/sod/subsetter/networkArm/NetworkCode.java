package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

public class NetworkCode implements NetworkIdSubsetter,SodElement {

	public NetworkCode(Element config) {
		System.out.println("The name of the element passed to NetworkCode is "+config.getTagName());
		System.out.println("The value of the Network Code is "+SodUtil.getNestedText(config));
	}

	public boolean accept(NetworkId e, CookieJar cookies) {

		return true;

	}

}
