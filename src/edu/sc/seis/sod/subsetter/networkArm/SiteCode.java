package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

public class SiteCode implements SiteIdSubsetter {

	public SiteCode(Element config) {
	    this.config = config;
	
	}

	public boolean accept(NetworkAccessOperations network, SiteId siteId, CookieJar cookies) {
	    System.out.println("The site code is  "+SodUtil.getNestedText(config));
	    if(siteId.site_code.equals(SodUtil.getNestedText(config))) return true;
	    else return false;

	}

    private Element config = null;


}
