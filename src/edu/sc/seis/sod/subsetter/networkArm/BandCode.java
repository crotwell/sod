package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

public class BandCode implements ChannelSubsetter {
    public BandCode(Element config) {
        acceptedBand = SodUtil.getNestedText(config).charAt(0);

    }

    public boolean accept(Channel channel){
        return channel.get_id().channel_code.charAt(0) == acceptedBand;
    }

    private char acceptedBand;
}//BandCode
