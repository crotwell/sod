package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

public class SiteCode implements SiteIdSubsetter {

	public SiteCode(Element config) {

		System.out.println("The site code is  "+SodUtil.getNestedText(config));
	}

	public boolean accept(SiteId siteId, CookieJar cookies) {

		return true;

	}


}
