package edu.sc.seis.sod.subsetter.site;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;

public final class SiteNOT extends SiteLogicalSubsetter implements
        SiteSubsetter {

    public SiteNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Site e) throws Exception {
        Iterator it = subsetters.iterator();
        if(it.hasNext()) {
            SiteSubsetter filter = (SiteSubsetter)it.next();
            if(filter.accept(e)) { return false; }
        }
        return true;
    }
}// SiteNOT
