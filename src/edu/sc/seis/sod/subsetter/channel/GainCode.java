package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 */
public class GainCode implements ChannelSubsetter {

    public GainCode(Element config) {
        acceptedGain = SodUtil.getNestedText(config).charAt(0);
    }

    public boolean accept(Channel channel, ProxyNetworkAccess network) {
        return channel.get_id().channel_code.charAt(1) == acceptedGain;
    }

    private char acceptedGain;
}//GainCode
