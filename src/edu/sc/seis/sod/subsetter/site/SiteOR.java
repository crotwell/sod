package edu.sc.seis.sod.subsetter.site;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;

public final class SiteOR extends SiteLogicalSubsetter implements SiteSubsetter {

    public SiteOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Site e) {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            SiteSubsetter filter = (SiteSubsetter)it.next();
            if(filter.accept(e)) { return true; }
        }
        return false;
    }
}// SiteOR
