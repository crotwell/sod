package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public class AlwaysSuccess extends ChannelLogicalSubsetter implements ChannelSubsetter {

    public AlwaysSuccess(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean isSuccess(StringTree[] reasons) {
        return true;
    }

    public boolean shouldContinue(StringTree result) {
        return result.isSuccess();
    }
}
