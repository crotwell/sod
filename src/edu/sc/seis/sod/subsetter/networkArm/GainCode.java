package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;
/**
 * sample xml file
 * &lt;broadband/&gt;
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class GainCode implements ChannelIdSubsetter {

    /**
     * Creates a new <code>GainCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public GainCode(Element config) {

		System.out.println("GainCode must be considered");
	}

    /**
     * Describe <code>accept</code> method here.
     *
     * @param channelId a <code>ChannelId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(ChannelId channelId, CookieJar cookies) {

		return true;

	}


}//GainCode
