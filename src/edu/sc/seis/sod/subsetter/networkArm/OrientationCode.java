package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;
/**
 * sample xml file
 * <pre>
 * &lt;orientationCode&gt;&lt;value&gt;Z &lt;/value&gt;&lt;/orientationCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class OrientationCode implements ChannelSubsetter {

    /**
     * Creates a new <code>OrientationCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public OrientationCode(Element config) {
    this.config = config;
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAccess network, Channel channel, CookieJar cookies) throws Exception {
        if(channel.get_id().channel_code.charAt(2) == SodUtil.getNestedText(config).charAt(0)) {
        return true;
    }
    else return false;
    }

    private Element config;

}//OrientationCode
