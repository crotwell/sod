package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.subsetter.AreaSubsetter;
import org.w3c.dom.Element;

/**
 * SiteArea.java Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version This class is used to represent the subsetter SiteArea. Site Area
 *          implements SiteSubsetter and can be any one of GlobalArea or BoxArea
 *          or PointDistanceArea or FlinneEngdahlArea.
 */
public class SiteArea extends AreaSubsetter implements SiteSubsetter, SodElement {

    public SiteArea(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Site e, NetworkAccess network) {
        return super.accept(e.my_location);
    }
}
