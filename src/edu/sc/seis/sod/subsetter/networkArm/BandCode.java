package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.subsetter.networkArm.ChannelSubsetter;
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
    public BandCode(Element config) { this.config = config; }

    public boolean accept(Channel channel) throws Exception {
        if(channel.get_id().channel_code.charAt(0) == SodUtil.getNestedText(config).charAt(0)) {
            return true;
        }
        else return false;

    }

    private Element config;

}//BandCode
