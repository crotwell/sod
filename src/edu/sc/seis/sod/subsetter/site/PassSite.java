package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.Site;

/**
 * PassSiteSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  PassSite implements SiteSubsetter{

    public boolean accept(Site site) { return true;}

}// NullSiteSubsetter
