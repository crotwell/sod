package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class ChannelCode implements ChannelSubsetter {
    public ChannelCode(Element config) {
        acceptedCode = SodUtil.getNestedText(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network){
        return new StringTreeLeaf(this, channel.get_id().channel_code.equals(acceptedCode));
    }

    private String acceptedCode;
}//GainCode
