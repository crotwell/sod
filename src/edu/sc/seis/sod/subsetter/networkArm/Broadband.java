package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

public class Broadband implements ChannelIdSubsetter {

	public Broadband(Element config) {

		System.out.println("Broadband must be considered");
	}

	public boolean accept(ChannelId channelId, CookieJar cookies) {

		return true;

	}


}//Broadband
