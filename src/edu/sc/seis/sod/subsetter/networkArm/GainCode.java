package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
/**
 * sample xml file
 * <pre>
 * &lt;gainCode&gt;&lt;value&gt;Z &lt;/value&gt;&lt;/gainCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class GainCode implements ChannelSubsetter {
    public GainCode(Element config) {
        this.config = config;
    }

    public boolean accept(Channel channel) throws Exception {
        if(channel.get_id().channel_code.charAt(1) == SodUtil.getNestedText(config).charAt(0)) {
            return true;
        } else return false;

    }

    private Element config;
}//GainCode
