package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author Srinivasa Telukutla
 */
public class GainCode implements ChannelSubsetter {

    public GainCode(Element config) {
        acceptedGain = SodUtil.getNestedText(config).charAt(0);
    }

    public StringTree accept(Channel channel, NetworkSource network) {
        return new StringTreeLeaf(this, channel.getChannelCode().charAt(1) == acceptedGain);
    }

    private char acceptedGain;
}//GainCode
