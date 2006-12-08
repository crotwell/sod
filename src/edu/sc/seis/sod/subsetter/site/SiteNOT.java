package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public final class SiteNOT extends SiteLogicalSubsetter implements
        SiteSubsetter {

    public SiteNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean isSuccess(StringTree[] reasons) {
        return !reasons[0].isSuccess();
    }

    public boolean shouldContinue(StringTree result) {
        return false;
    }
}// SiteNOT
