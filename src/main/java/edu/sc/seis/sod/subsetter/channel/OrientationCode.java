package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OrientationCode implements ChannelSubsetter {
    public OrientationCode(Element config) {
        acceptedOrientation = SodUtil.getNestedText(config).charAt(0);
    }

    public StringTree accept(Channel channel, NetworkSource network){
        return new StringTreeLeaf(this, channel.getChannelCode().charAt(2) == acceptedOrientation);
    }

    private char acceptedOrientation;
}//OrientationCode
