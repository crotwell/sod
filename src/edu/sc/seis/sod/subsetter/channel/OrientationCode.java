package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

public class OrientationCode implements ChannelSubsetter {
    public OrientationCode(Element config) {
        acceptedOrientation = SodUtil.getNestedText(config).charAt(0);
    }

    public boolean accept(Channel channel){
        return channel.get_id().channel_code.charAt(2) == acceptedOrientation;
    }

    private char acceptedOrientation;
}//OrientationCode
