package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class BandCode implements ChannelSubsetter {
    public BandCode(Element config) {
        acceptedBand = SodUtil.getNestedText(config).charAt(0);

    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network){
        return new StringTreeLeaf(this, channel.get_id().channel_code.charAt(0) == acceptedBand);
    }

    private char acceptedBand;
}//BandCode
