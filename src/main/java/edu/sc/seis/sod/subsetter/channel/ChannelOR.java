package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public  class ChannelOR extends ChannelLogicalSubsetter implements
        ChannelSubsetter {

    public ChannelOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean shouldContinue(StringTree result) {
        return !result.isSuccess();
    }

    public boolean isSuccess(StringTree[] reasons) {
        for(int i = 0; i < reasons.length; i++) {
            if(reasons[i].isSuccess()){
                return true;
            }
        }
        return false;
    }
}// ChannelOR
