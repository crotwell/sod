package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public final class StationAND extends StationLogicalSubsetter implements
        StationSubsetter {

    public StationAND(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean shouldContinue(StringTree result) {
        return result.isSuccess();
    }

    public boolean isSuccess(StringTree[] reasons) {
        for(int i = 0; i < reasons.length; i++) {
            if(!reasons[i].isSuccess()){
                return false;
            }
        }
        return true;
    }
}// StationAND
