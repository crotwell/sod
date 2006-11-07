package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class OrientationCode implements ChannelSubsetter {
    public OrientationCode(Element config) {
        acceptedOrientation = SodUtil.getNestedText(config).charAt(0);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network){
        return new StringTreeLeaf(this, channel.get_id().channel_code.charAt(2) == acceptedOrientation);
    }

    private char acceptedOrientation;
}//OrientationCode
