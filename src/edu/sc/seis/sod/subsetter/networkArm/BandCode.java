package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.subsetter.networkArm.ChannelSubsetter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
/**
 * sample xml file
 * <pre>
 * &lt;bandcode&gt;&lt;value&gt;B&lt;/value&gt;&lt;/bandCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class BandCode implements ChannelSubsetter {

    /**
     * Creates a new <code>BandCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public BandCode(Element config) {
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
    if(channel.get_id().channel_code.charAt(0) == SodUtil.getNestedText(config).charAt(0)) {
        return true;
    }
    else return false;

    }

    private Element config;

}//BandCode
