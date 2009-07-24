package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public final class NetworkNOT extends  NetworkLogicalSubsetter
    implements NetworkSubsetter {

    public NetworkNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean isSuccess(StringTree[] reasons) {
        return !reasons[0].isSuccess();
    }

    public boolean shouldContinue(StringTree result) {
        return false;
    }
    
}// NetworkAttrNOT
