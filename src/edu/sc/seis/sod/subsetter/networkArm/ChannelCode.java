package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

public class ChannelCode implements ChannelSubsetter {
    public ChannelCode(Element config) {
        acceptedCode = SodUtil.getNestedText(config);
    }

    public boolean accept(Channel channel){
        return channel.get_id().channel_code.equals(acceptedCode);
    }

    private String acceptedCode;
}//GainCode
