package edu.sc.seis.sod.subsetter.channel;

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
        acceptedGain = SodUtil.getNestedText(config).charAt(0);
    }

    public boolean accept(Channel channel){
        return channel.get_id().channel_code.charAt(1) == acceptedGain;
    }

    private char acceptedGain;
    private Element config;
}//GainCode
