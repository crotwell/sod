package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public final class ChannelXOR extends ChannelLogicalSubsetter implements
        ChannelSubsetter {

    public ChannelXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean isSuccess(StringTree[] reasons) {
        return reasons[0].isSuccess() != reasons[1].isSuccess();
    }

    public boolean shouldContinue(StringTree result) {
        return true;
    }
}// ChannelXOR
